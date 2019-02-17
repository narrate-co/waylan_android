package space.narrate.words.android.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.R
import space.narrate.words.android.billing.BillingConfig
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.data.firestore.users.oneDayPastExpiration
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.configError
import space.narrate.words.android.util.configInformative
import kotlinx.android.synthetic.main.fragment_developer_settings.*
import kotlinx.android.synthetic.main.fragment_developer_settings.view.*


/**
 * A Fragment to expose preferences to manipulate the configuration of Words, variables used
 * in conjunction with the current user (ie. directly changing Merriam-Webster PluginState
 * properties, clearing [UserPreferences]) and UI/UX elements (like Snackbar example triggers)
 *
 * [R.id.clearUser] Resets all [UserPreferences] to their defaults
 * [R.id.merriamWebsterState] Changes the [PluginState] of the current user's Merriam-Webster plugin
 *  by manipulating properties on [User]
 * [R.id.useTestSkus] Toggle to run Google Play Billing against test skus defined in [BillingConfig]
 * [R.id.merriamWebsterBillingResponse] Toggle to switch between the three possible "fake" billing
 *  skus available for testing Google Play Billing
 * [R.id.informativeSnackbar] Trigger an informative Snackbar to test UI/UX
 * [R.id.errorSnackbar] Trigger an error Snackbar to test UI/UX
 */
class DeveloperSettingsFragment : BaseUserFragment() {

    companion object {
        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "developer_fragment_tag"

        fun newInstance() = DeveloperSettingsFragment()
    }

    // Reuse the SettingsViewView model from SettingsFragment since many of the methods are the same
    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_developer_settings, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }
        setDeveloperSettings(view)
        return view
    }

    private fun setDeveloperSettings(view: View) {

        // Clear user preferences preference
        view.clearUser.setOnClickListener {
            viewModel.clearUserPreferences()
            Snackbar.make(view, "All user preferences cleared", Snackbar.LENGTH_SHORT)
                    .configInformative(context!!, false)
                    .show()
        }

        // Merriam-Webster state preference
        viewModel.userLive.observe(this, Observer { user ->
            val state = user.merriamWebsterState
            view.merriamWebsterState.setDesc(when (state) {
                is PluginState.None -> {
                    "None"
                }
                is PluginState.FreeTrial -> {
                    "Free trial (${if (state.isValid) "valid" else "expired"})"
                }
                is PluginState.Purchased -> {
                    "Purchased (${if (state.isValid) "valid" else "expired"})"
                }
            })
            view.merriamWebsterState.setOnClickListener {
                cycleState(user)
            }
        })

        // Use test skus preference
        viewModel.useTestSkusLive.observe(this, Observer {
            view.useTestSkus.setChecked(it)
        })
        view.useTestSkus.setOnClickListener {
            viewModel.useTestSkus = !viewModel.useTestSkus
        }

        // Merriam-Webster billing response preference
        view.merriamWebsterBillingResponse.setDesc(BillingConfig.TEST_SKU_MERRIAM_WEBSTER)
        view.merriamWebsterBillingResponse.setOnClickListener {
            val newResponse = cycleIabTestResponse(BillingConfig.TEST_SKU_MERRIAM_WEBSTER)
            BillingConfig.TEST_SKU_MERRIAM_WEBSTER = newResponse
            view.merriamWebsterBillingResponse.setDesc(newResponse)
        }

        // Informative Snackbar preference
        view.informativeSnackbar.setOnClickListener {
            Snackbar.make(settingsRoot, "A test piece of information", Snackbar.LENGTH_LONG)
                    .setAction("Okay") {
                        Log.d("DevSettings", "Informative snackbard clicked!")
                    }
                    .configInformative(context!!, false)
                    .show()
        }

        // Error Snackbar preference
        view.errorSnackbar.setOnClickListener {
            Snackbar.make(settingsRoot, "A test error", Snackbar.LENGTH_LONG)
                    .setAction("Report") {
                        Log.d("DevSettings", "Error snackbar clicked")
                    }
                    .configError(context!!, false)
                    .show()
        }
    }

    /**
     * Cycles through the 4 (excluding NONE) different configurations of all PluginStates.
     *
     * The cycled states should be:
     *  FREE_TRIAL (valid)
     *  FREE_TRIAL (expired)
     *  SUBSCRIBED (valid)
     *  SUBSCRIBED (expired)
     */
    private fun cycleState(user: User) {
        val state = user.merriamWebsterState
        viewModel.setMerriamWebsterState(
                when {
                    //None -> Free Trial (valid)
                    state is PluginState.None -> {
                        PluginState.FreeTrial(user.isAnonymous)
                    }
                    //FreeTrial (valid) -> FreeTrial (expired)
                    state is PluginState.FreeTrial && state.isValid -> {
                        PluginState.FreeTrial(user.isAnonymous, user.oneDayPastExpiration)
                    }
                    //FreeTrial (expired) -> Purchased (valid)
                    state is PluginState.FreeTrial && !state.isValid -> {
                        PluginState.Purchased(purchaseToken = user.merriamWebsterPurchaseToken)
                    }
                    //Purchased (valid) -> Purchased (expired)
                    state is PluginState.Purchased && state.isValid -> {
                        PluginState.Purchased(
                                user.oneDayPastExpiration,
                                user.merriamWebsterPurchaseToken
                        )
                    }
                    //Purchased (expired) -> FreeTrial (valid)
                    state is PluginState.Purchased && !state.isValid -> {
                        PluginState.FreeTrial(user.isAnonymous)
                    }
                    //Default
                    else -> PluginState.None()
                })
    }

    /**
     * Cycles through the 3 available test skus to test Google Play Billing against
     */
    private fun cycleIabTestResponse(currentResponse: String): String {
        return when (currentResponse) {
            BillingConfig.TEST_SKU_PURCHASED -> BillingConfig.TEST_SKU_CANCELED
            BillingConfig.TEST_SKU_CANCELED -> BillingConfig.TEST_SKU_ITEM_UNAVAILABLE
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> BillingConfig.TEST_SKU_PURCHASED
            else -> BillingConfig.TEST_SKU_PURCHASED
        }
    }

}
