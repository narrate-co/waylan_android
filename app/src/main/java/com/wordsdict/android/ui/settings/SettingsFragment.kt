package com.wordsdict.android.ui.settings

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.wordsdict.android.*
import com.wordsdict.android.billing.BillingConfig
import com.wordsdict.android.billing.BillingManager
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.ui.auth.AuthActivity
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.configError
import kotlinx.android.synthetic.main.fragment_settings.view.*
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.merriamWebsterState
import com.wordsdict.android.ui.dialog.RoundedAlertDialog
import com.wordsdict.android.util.visible
import javax.inject.Inject


class SettingsFragment : BaseUserFragment() {

    @Inject
    lateinit var billingManger: BillingManager

    companion object {
        const val FRAGMENT_TAG = "settings_fragment_tag"
        fun newInstance() = SettingsFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }
        viewModel.getUserLive().observe(this, Observer {
            setSettings(view, it)
        })
        return view
    }

    private fun setSettings(view: View, user: User?) {
        //set state specific settings
        when {
            user == null || user.isAnonymous -> setAsAnonymous(view, user)
            else -> setAsRegistered(view, user)
        }

        //set common settings
        //night mode
        val nightModeCallback = object: RoundedAlertDialog.NightModeCallback() {
            var selected: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            override fun onSelected(nightMode: Int) {
                selected = nightMode
            }

            override fun onDismissed() {
                viewModel.nightMode = selected
                (activity as? SettingsActivity)?.updateNightMode(selected)
            }
        }

        viewModel.nightModeLive.observe(this, Observer {
            val desc = when (it) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> getString(R.string.settings_night_mode_follows_system_title)
                AppCompatDelegate.MODE_NIGHT_AUTO -> getString(R.string.settings_night_mode_auto_title)
                AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.settings_night_mode_yes_title)
                AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.settings_night_mode_no_title)
                else -> getString(R.string.settings_night_mode_follows_system_title)
            }
            view.nightMode.setDesc(desc)
        })

        view.nightMode.setOnClickListener {
            RoundedAlertDialog.newNightModeInstance(viewModel.nightMode, nightModeCallback).show(activity?.supportFragmentManager, "night_mode")
        }

        // about
        view.about.setOnClickListener {
            (activity as? SettingsActivity)?.showAbout()
        }

        // contact
        view.contact.setShowDivider(BuildConfig.DEBUG) // if debug, there will be a developer settings tile after this. Show divider
        view.contact.setOnClickListener {
            try {
                Navigator.launchEmail(context!!, Config.SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(view.settingsRoot, getString(R.string.settings_email_compose_no_client_error), Snackbar.LENGTH_SHORT)
                        .configError(context!!, false)
                        .show()
            }
        }

        // developer settings
        view.developer.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        view.developer.setOnClickListener {
            (activity as? SettingsActivity)?.showDeveloperSettings()
        }
    }

    private fun setAsAnonymous(view: View, user: User?) {
        val state = user?.merriamWebsterState ?: PluginState.None()
        when (state) {
            is PluginState.None -> {
                view.banner.setBannerText(getString(R.string.settings_header_anonymous_none_body))
                view.banner.setBannerLabelText(null)
            }
            is PluginState.FreeTrial -> {
                val label = if (state.isValid) "Free Trial: ${state.remainingDays}d" else "Free trial expired"
                view.banner.setBannerLabelText(label)
                view.banner.setBannerText(getString(R.string.settings_header_anonymous_free_trial_body))
            }
            is PluginState.Purchased -> {
                //This should never happen
                view.banner.setBannerText(getString(R.string.settings_header_anonymous_none_body))
                view.banner.setBannerLabelText(null)
            }
        }


        view.banner.setBannerTopButton(getString(R.string.settings_header_anonymous_create_account_button), View.OnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.SIGN_UP)
        })

        view.banner.setBannerBottomButton(getString(R.string.settings_header_anonymous_log_in_button), View.OnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        })

        view.banner.visible()
    }

    private fun setAsRegistered(view: View, user: User) {
        val state = user.merriamWebsterState
        when (state) {
            is PluginState.None -> {
                view.banner.setBanner(
                        text = getString(R.string.settings_header_registered_none_body),
                        topButton = getString(R.string.settings_header_registered_add_button)
                )
            }
            is PluginState.FreeTrial -> {
                if (state.isValid) {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_free_trial_body),
                            label = "Free trial: ${state.remainingDays}d",
                            topButton = getString(R.string.settings_header_registered_add_button),
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                } else {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_free_trial_expired_body),
                            label = "Free trial expired",
                            topButton = getString(R.string.settings_header_registered_add_button),
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                }
            }
            is PluginState.Purchased -> {
                if (state.isValid) {

                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_subscribed_body),
                            label = "Added"
                    )
                } else {

                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_subscribed_expired_body),
                            label = "Plugin expired",
                            topButton = "Renew",
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                }

            }
        }

        //sign out
        if (user.email.isNotBlank()) {
            view.signOut.setDesc(user.email)
        }
        view.signOut.setOnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        }
    }

    private fun launchMerriamWebsterPurchaseFlow() {
        billingManger.initiatePurchaseFlow(activity!!, BillingConfig.SKU_MERRIAM_WEBSTER)
    }

}
