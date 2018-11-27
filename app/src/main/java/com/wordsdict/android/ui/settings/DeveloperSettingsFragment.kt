package com.wordsdict.android.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.wordsdict.android.R
import com.wordsdict.android.billing.BillingConfig
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.firestore.users.merriamWebsterState
import com.wordsdict.android.data.firestore.users.oneDayPastExpiration
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.configError
import com.wordsdict.android.util.configInformative
import kotlinx.android.synthetic.main.fragment_developer_settings.*
import kotlinx.android.synthetic.main.fragment_developer_settings.view.*


class DeveloperSettingsFragment : BaseUserFragment() {

    companion object {
        const val FRAGMENT_TAG = "developer_fragment_tag"
        fun newInstance() = DeveloperSettingsFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_developer_settings, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }
        setDeveloperSettings(view)
        return view
    }

    private fun setDeveloperSettings(view: View) {
        view.clearUser.setOnClickListener {
            viewModel.clearUserPreferences()
            Snackbar.make(view, "All user preferences cleared", Snackbar.LENGTH_SHORT)
                    .configInformative(context!!, false)
                    .show()
        }

        viewModel.getUserLive().observe(this, Observer { user ->
            val state = user.merriamWebsterState
            view.merriamWebsterState.setDesc(when (state) {
                is PluginState.None -> "None"
                is PluginState.FreeTrial -> "Free trial (${if (state.isValid) "valid" else "expired"})"
                is PluginState.Purchased -> "Purchased (${if (state.isValid) "valid" else "expired"})"
            })
            view.merriamWebsterState.setOnClickListener {
                cycleState(user)
            }
        })

        viewModel.useTestSkusLive.observe(this, Observer {
            view.useTestSkus.setChecked(it)
        })
        view.useTestSkus.setOnClickListener {
            viewModel.useTestSkus = !viewModel.useTestSkus
        }

        view.merriamWebsterBillingResponse.setDesc(BillingConfig.TEST_SKU_MERRIAM_WEBSTER)
        view.merriamWebsterBillingResponse.setOnClickListener {
            val newResponse = cycleIabTestResponse(BillingConfig.TEST_SKU_MERRIAM_WEBSTER)
            BillingConfig.TEST_SKU_MERRIAM_WEBSTER = newResponse
            view.merriamWebsterBillingResponse.setDesc(newResponse)
        }

        view.informativeSnackbar.setOnClickListener {
            Snackbar.make(settingsRoot, "A test piece of information", Snackbar.LENGTH_LONG)
                    .setAction("Okay") {
                        Log.d("DevSettings", "Informative snackbard clicked!")
                    }
                    .configInformative(context!!, false)
                    .show()
        }

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
     * Should cycle through:
     *
     * NONE
     * FREE_TRIAL (VALID)
     * FREE_TRIAL (expired)
     * SUBSCRIBED (valid)
     * SUBSCRIBED (expired)
     */
    private fun cycleState(user: User) {
        val state = user.merriamWebsterState
        viewModel.setMerriamWebsterState(
                when {
                    //None -> Free Trial (valid)
                    state is PluginState.None -> PluginState.FreeTrial(user.isAnonymous)
                    //FreeTrial (valid) -> FreeTrial (expired)
                    state is PluginState.FreeTrial && state.isValid -> PluginState.FreeTrial(user.isAnonymous, user.oneDayPastExpiration)
                    //FreeTrial (expired) -> Purchased (valid)
                    state is PluginState.FreeTrial && !state.isValid -> PluginState.Purchased(purchaseToken = user.merriamWebsterPurchaseToken)
                    //Purchased (valid) -> Purchased (expired)
                    state is PluginState.Purchased && state.isValid -> PluginState.Purchased(user.oneDayPastExpiration, user.merriamWebsterPurchaseToken)
                    //Purchased (expired) -> FreeTrial (valid)
                    state is PluginState.Purchased && !state.isValid -> PluginState.FreeTrial(user.isAnonymous)
                    //Default
                    else -> PluginState.None()
                })
    }

    private fun cycleIabTestResponse(currentResponse: String): String {
        return when (currentResponse) {
            BillingConfig.TEST_SKU_PURCHASED -> BillingConfig.TEST_SKU_CANCELED
            BillingConfig.TEST_SKU_CANCELED -> BillingConfig.TEST_SKU_ITEM_UNAVAILABLE
            BillingConfig.TEST_SKU_ITEM_UNAVAILABLE -> BillingConfig.TEST_SKU_PURCHASED
            else -> BillingConfig.TEST_SKU_PURCHASED
        }
    }

}
