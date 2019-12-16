package space.narrate.waylan.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import java.util.*

/**
 * A class to handle all communication between Words and Google Play Billing
 *
 * This is a modified version of TrivialDrive 2's BillingManager.
 *
 * Billing works by making a one-time purchase for a plugin, setting the
 * [User.merriamWebsterPurchaseToken] and [User.merriamWebsterStarted] variables in Firebase and
 * then immediately consuming the purchase.
 *
 * The reason for this is because Words plugins are meant to be lightweight purchases that should
 * avoid making the user contemplate a commitment. In an effort to achieve this, Words plugins are
 * one-time purchases that are good for one year (365 days). Once a year elapses, the plugin is
 * expired and [PluginState.isValid]' should return false.
 *
 * @param userRepository Used to update Firestore [User] objects with purchaseToken and
 *      start dates
 **
 */
class BillingManager(
    private val context: Context,
    private val userRepository: UserRepository,
    private val analyticsRepository: AnalyticsRepository
): PurchasesUpdatedListener {

    companion object {
        private const val BILLING_MANAGER_NOT_INITIALIZED = -1
        private const val BASE_64_ENCODED_PUBLIC_KEY = "EMPTY_FOR_NOW"
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(context).setListener(this).build()
    }

    private var isServiceConnected = false
    private var billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED

    private val tokensToBeConsumed = HashSet<String>()

    private val workQueue = PriorityQueue<BillingTask>()

    init {
        doWithServiceConnection {
            queryPurchasesAndSubscription(it, true)
        }
    }

    fun initiatePurchaseFlow(
        activity: Activity,
        skuId: String,
        @BillingClient.SkuType billingType: String = BillingClient.SkuType.INAPP
    ) {
//        val sku = if (userRepository.useTestSkus) {
//            when (skuId) {
//                BillingConfig.SKU_MERRIAM_WEBSTER -> BillingConfig.TEST_SKU_MERRIAM_WEBSTER
//                else -> BillingConfig.TEST_SKU_PURCHASED
//            }
//        } else {
//            skuId
//        }
        initiatePurchaseFlow(activity, skuId, null, billingType)
    }

    private fun initiatePurchaseFlow(
        activity: Activity,
        skuId: String,
        oldSkus: ArrayList<String>?,
        @BillingClient.SkuType billingType: String
    ) {
        doWithServiceConnection {
            val purchaseParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(billingType)
                .setOldSkus(oldSkus)
                .build()
            it.launchBillingFlow(activity, purchaseParams)
        }
    }

    fun destroy() {
        if (billingClient != null) {
            billingClient.endConnection()
        }
    }

    fun querySkuDetails(
        @BillingClient.SkuType itemType: String,
        skuList: List<String>,
        listener: SkuDetailsResponseListener
    ) {
        doWithServiceConnection {
            val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(itemType)
            it.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
                listener.onSkuDetailsResponse(responseCode, skuDetailsList)
            }
        }
    }

    fun consume(purchaseToken: String) {
        if (tokensToBeConsumed.contains(purchaseToken)) {
            return
        }

        tokensToBeConsumed.add(purchaseToken)

        val onConsumeListener = ConsumeResponseListener { _, token ->
            tokensToBeConsumed.remove(token)
        }

        doWithServiceConnection {
            it.consumeAsync(purchaseToken, onConsumeListener)
        }
    }

    private fun areSubscriptionsSupported(): Boolean {
        return billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) ==
            BillingClient.BillingResponse.OK
    }

    /**
     * Gets all the current active, purchases and subs (non-consumed, non-cancelled, non-expired)
     */
    private fun queryPurchasesAndSubscription(client: BillingClient, consumeAll: Boolean) {
        val purchaseResults = client.queryPurchases(BillingClient.SkuType.INAPP)

        if (areSubscriptionsSupported()) {
            val subscriptionResult = client.queryPurchases(BillingClient.SkuType.SUBS)
            if (subscriptionResult.responseCode == BillingClient.BillingResponse.OK) {
                purchaseResults.purchasesList.addAll(subscriptionResult.purchasesList)
            }
        }

        onQueryPurchasesFinished(purchaseResults, consumeAll)
    }

    override fun onPurchasesUpdated(resultCode: Int, purchases: MutableList<Purchase>?) {
        if (resultCode == BillingClient.BillingResponse.OK && purchases != null) {
            purchases.forEach {
                handlePurchase(it)
            }
        } else if (resultCode == BillingClient.BillingResponse.USER_CANCELED) {
            // the user canceled the purchase flow
        } else {
            // a different response was returned
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        //TODO Make sure we're verifying purchase tokens in a Cloud Function

        val startedDate = Date(purchase.purchaseTime)

        when (purchase.sku) {
            BillingConfig.SKU_MERRIAM_WEBSTER -> {
                val pluginState = PluginState.Purchased(startedDate, purchase.purchaseToken)
                userRepository.setUserMerriamWebsterState(pluginState)
            }
            BillingConfig.SKU_MERRIAM_WEBSTER_THESAURUS -> {
                val pluginState = PluginState.Purchased(startedDate, purchase.purchaseToken)
                userRepository.setUserMerriamWebsterThesaurusState(pluginState)
            }
            BillingConfig.TEST_SKU_MERRIAM_WEBSTER,
            BillingConfig.TEST_SKU_PURCHASED,
            BillingConfig.TEST_SKU_CANCELED,
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> {
                val pluginState = PluginState.Purchased(Date(), purchase.purchaseToken)
                userRepository.setUserMerriamWebsterState(pluginState)
                analyticsRepository.logMerriamWebsterPurchaseEvent()
            }
        }

        consume(purchase.purchaseToken)
    }

    private fun onQueryPurchasesFinished(result: Purchase.PurchasesResult, consumeAll: Boolean) {
        if (billingClient == null || result.responseCode != BillingClient.BillingResponse.OK) {
            return
        }

        if (consumeAll) {
            result.purchasesList.forEach {
                consume(it.purchaseToken)
            }
        } else {
            onPurchasesUpdated(BillingClient.BillingResponse.OK, result.purchasesList)
        }
    }

    private fun doWithServiceConnection(task: (BillingClient) -> Unit) {
        doWithServiceConnection(BillingTask(task))
    }

    private fun doWithServiceConnection(task: BillingTask) {
        // TODO: Replace tasks which have the same id
        workQueue.offer(task)

        if (isServiceConnected) {
            workQueue.pollEach { it.run(billingClient) }
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(responseCode: Int) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        isServiceConnected = true
                        workQueue.pollEach { it.run(billingClient) }
                        billingClientResponseCode = responseCode
                    }
                }
                override fun onBillingServiceDisconnected() {
                    isServiceConnected = false
                }
            })
        }
    }

    /**
     * Copy a queue and immediately clear it. For each item in the copied queue, run [action].
     *
     * This is used to immediately run all queued work to be done with a [BillingClient] and avoid
     * any work being duplicated by subsequent calls to [doWithServiceConnection].
     */
    private fun <E> PriorityQueue<E>.pollEach(action: (E) -> Unit) {
        val items = PriorityQueue(this)
        clear()
        do {
            action(items.poll())
        } while (items.isNotEmpty())
    }
}