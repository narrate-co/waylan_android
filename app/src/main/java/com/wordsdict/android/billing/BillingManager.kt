package com.wordsdict.android.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.util.TimeUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.api.Billing
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class BillingManager(
        private val context: Context,
        private val preferenceRepository: PreferenceRepository,
        private val userRepository: UserRepository
): PurchasesUpdatedListener {

    companion object {
        private val TAG = BillingManager::class.java.simpleName

        private const val BILLING_MANAGER_NOT_INITIALIZED = -1
        private const val BASE_64_ENCODED_PUBLIC_KEY = "EMPTY_FOR_NOW"
    }

    private val billingClient by lazy { BillingClient.newBuilder(context).setListener(this).build() }

    private var isServiceConnected = false
    private var billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED

    private val tokensToBeConsumed = HashSet<String>()

    init {
        println("$TAG::init")
        doWithServiceConnection {
            launch(UI) {
                it.queryPurchasesAndSubscription(true)
            }
        }
    }

    fun initiatePurchaseFlow(activity: Activity, skuId: String, @BillingClient.SkuType billingType: String = BillingClient.SkuType.INAPP) {
        val sku = if (preferenceRepository.useTestSkus) {
            when (skuId) {
                BillingConfig.SKU_MERRIAM_WEBSTER -> BillingConfig.TEST_SKU_MERRIAM_WEBSTER
                else -> BillingConfig.TEST_SKU_PURCHASED
            }
        } else {
            skuId
        }
        initiatePurchaseFlow(activity, sku, null, billingType)
    }

    fun initiatePurchaseFlow(activity: Activity, skuId: String, oldSkus: ArrayList<String>?, @BillingClient.SkuType billingType: String) {
        doWithServiceConnection {
            launch(UI) {
                val purchaseParams = BillingFlowParams.newBuilder()
                        .setSku(skuId)
                        .setType(billingType)
                        .setOldSkus(oldSkus)
                        .build()
                it.launchBillingFlow(activity, purchaseParams)
            }
        }
    }

    fun destroy() {
        if (billingClient != null) {
            billingClient.endConnection()
        }
    }

    fun querySkuDetails(@BillingClient.SkuType itemType: String, skuList: List<String>, listener: SkuDetailsResponseListener) {
        doWithServiceConnection {
            launch(UI) {
                val params = SkuDetailsParams.newBuilder()
                        .setSkusList(skuList)
                        .setType(itemType)
                it.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
                    listener.onSkuDetailsResponse(responseCode, skuDetailsList)
                }
            }
        }
    }

    private fun doWithServiceConnection(work: (BillingClient) -> Unit) {
        println("$TAG::doWithServiceConnection isServiceConnected = $isServiceConnected")
        if (isServiceConnected) {
            launch(UI) {
                work(billingClient)
            }
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(responseCode: Int) {
                    println("$TAG::doWithService::onBillingSetupFinished")
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        isServiceConnected = true
                        launch(UI) {
                            work(billingClient)
                        }
                        billingClientResponseCode = responseCode
                    }
                }
                override fun onBillingServiceDisconnected() {
                    println("$TAG::doWithService::onBillingServiceDisconnected")
                    isServiceConnected = false
                }
            })
        }
    }


    fun consume(purchaseToken: String) {
        println("$TAG::consume $purchaseToken")
        if (tokensToBeConsumed.contains(purchaseToken)) {
            println("$TAG::consume - tokenIsAlready in tokensToBeConsumed $purchaseToken")
            return
        }

        tokensToBeConsumed.add(purchaseToken)

        val onConsumeListener = ConsumeResponseListener { responseCode, purchaseToken ->
            println("$TAG::consume - onConsumeListener $purchaseToken")
            tokensToBeConsumed.remove(purchaseToken)
        }

        doWithServiceConnection {
            launch(UI) {
                println("$TAG::consumeAsync")
                it.consumeAsync(purchaseToken, onConsumeListener)
            }
        }
    }

    private fun areSubscriptionsSupported(): Boolean {
        return billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingClient.BillingResponse.OK
    }

    /**
     * Gets all the current active, purchases and subs (non-consumed, non-cancelled, non-expired)
     */
    private fun BillingClient.queryPurchasesAndSubscription(consumeAll: Boolean) {
        val purchaseResults = queryPurchases(BillingClient.SkuType.INAPP)

        if (areSubscriptionsSupported()) {
            val subscriptionResult = queryPurchases(BillingClient.SkuType.SUBS)
            if (subscriptionResult.responseCode == BillingClient.BillingResponse.OK) {
                purchaseResults.purchasesList.addAll(subscriptionResult.purchasesList)
            }
        }

        onQueryPurchasesFinished(purchaseResults, consumeAll)
    }

    override fun onPurchasesUpdated(resultCode: Int, purchases: MutableList<Purchase>?) {
        if (resultCode == BillingClient.BillingResponse.OK && purchases != null) {
            println("$TAG::onPurchasesUpdated::OK - $purchases")
            purchases.forEach {
                handlePurchase(it)
            }
        } else if (resultCode == BillingClient.BillingResponse.USER_CANCELED) {
            // the user canceled the purchase flow
            println("$TAG::onPurchasesUpdated::Canceled - $purchases")
        } else {
            //TODO
            // and unknown error ocurred
            println("$TAG::onPurchasesUpdated::Other - $purchases, resultCode = $resultCode")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        //TODO add security checks
//        if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
//            println("$TAG::handlePurchase - not valid signature!")
//            return
//        }

        //TODO parse purchase and update User with userRepository
//        val startedDate = Date(TimeUnit.MILLISECONDS.convert(purchase.purchaseTime, TimeUnit.SECONDS))
        val startedDate = Date(purchase.purchaseTime)
        println("$TAG::handlePurchase - start $startedDate, json ${purchase.originalJson}")

        when (purchase.sku) {
            BillingConfig.SKU_MERRIAM_WEBSTER -> {
                val pluginState = PluginState.Purchased(startedDate, purchase.purchaseToken)
                userRepository.setUserMerriamWebsterState(pluginState)
            }
            BillingConfig.TEST_SKU_MERRIAM_WEBSTER,
            BillingConfig.TEST_SKU_PURCHASED,
            BillingConfig.TEST_SKU_CANCELED,
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> {
                val pluginState = PluginState.Purchased(Date(), purchase.purchaseToken)
                userRepository.setUserMerriamWebsterState(pluginState)
            }
        }

        consume(purchase.purchaseToken)
    }

    private fun onQueryPurchasesFinished(result: Purchase.PurchasesResult, consumeAll: Boolean) {
        println("$TAG::onQueryPurchasesFinished consumeAll: $consumeAll, purchases: ${result.purchasesList}")
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


    //TODO this should happen in Cloud Function
    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            Security.verifyPurchase(BASE_64_ENCODED_PUBLIC_KEY, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

}