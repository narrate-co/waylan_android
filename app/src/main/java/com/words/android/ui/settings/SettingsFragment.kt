package com.words.android.ui.settings

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.words.android.*
import com.words.android.data.firestore.users.User
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.merriamWebsterState
import com.words.android.ui.dialog.RoundedAlertDialog
import com.words.android.util.invisible


class SettingsFragment : BaseUserFragment() {

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
        view.navigationIcon.setOnClickListener { activity?.onBackPressed() }
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
        view.darkModeSettings.settingsTitle.text = getString(R.string.settings_night_mode_title)
        viewModel.nightModeLive.observe(this, Observer {
            val desc = when (it) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "Follows system"
                AppCompatDelegate.MODE_NIGHT_AUTO -> "Auto"
                AppCompatDelegate.MODE_NIGHT_YES -> "Yes"
                AppCompatDelegate.MODE_NIGHT_NO -> "No"
                else -> "Follows system"
            }

            view.darkModeSettings.settingsDescription.text = desc

        })

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

        view.darkModeSettings.checkbox.invisible()
        view.darkModeSettings.settingsItem.setOnClickListener {
            RoundedAlertDialog.newNightModeInstance(viewModel.nightMode, nightModeCallback).show(activity?.supportFragmentManager, "night_mode")
        }

        view.aboutSetting.settingsTitle.text = getString(R.string.settings_about_title)
        view.aboutSetting.settingsDescription.text = getString(R.string.settings_about_desc)
        view.aboutSetting.checkbox.visibility = View.INVISIBLE
        view.aboutSetting.setOnClickListener {
            (activity as? SettingsActivity)?.showAbout()
        }

        view.contactSetting.settingsTitle.text = getString(R.string.settings_contact_title)
        view.contactSetting.settingsDescription.text = getString(R.string.settings_contact_desc)
        view.contactSetting.checkbox.visibility = View.INVISIBLE
        view.contactSetting.setOnClickListener {
            try {
                Navigator.launchEmail(context!!, Config.SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(view.settingsRoot, getString(R.string.settings_email_compose_no_client_error), Snackbar.LENGTH_SHORT)
                        .configError(context!!, false)
                        .show()
            }
        }

        view.developerSettings.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        view.developerSettings.checkbox.visibility = View.INVISIBLE
        view.developerSettings.settingsTitle.text = getString(R.string.settings_developer_title)
        view.developerSettings.settingsDescription.text = getString(R.string.settings_developer_desc)
        view.developerSettings.setOnClickListener {
            (activity as? SettingsActivity)?.showDeveloperSettings()
        }
    }

    private fun setAsAnonymous(view: View, user: User?) {
        val state = user?.merriamWebsterState ?: PluginState.None()
        when (state) {
            is PluginState.None -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_anonymous_none_body)
                view.accountDialogCard.textLabel.visibility = View.GONE
            }
            is PluginState.FreeTrial -> {
                val label = if (state.isValid) "Free Trial: ${state.remainingDays}d" else "Free trial expired"
                view.accountDialogCard.textLabel.text = label
                view.accountDialogCard.textLabel.visibility = View.VISIBLE
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_anonymous_free_trial_body)
            }
            is PluginState.Subscribed -> {
                //This should never happen
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_anonymous_none_body)
                view.accountDialogCard.textLabel.visibility = View.GONE
            }
        }


        view.accountDialogCard.topButton.text = getString(R.string.settings_header_anonymous_create_account_button)
        view.accountDialogCard.bottomButton.text = getString(R.string.settings_header_anonymous_log_in_button)
        view.accountDialogCard.topButton.setOnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.SIGN_UP)
        }
        view.accountDialogCard.bottomButton.setOnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        }

        view.accountDialogCard.topButton.visibility = View.VISIBLE
        view.accountDialogCard.bottomButton.visibility = View.VISIBLE
        view.accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsRegistered(view: View, user: User) {
        val state = user.merriamWebsterState
        when (state) {
            is PluginState.None -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_none_body)
                view.accountDialogCard.topButton.text = getString(R.string.settings_header_registered_add_button)
                view.accountDialogCard.topButton.visibility = View.VISIBLE
                view.accountDialogCard.textLabel.visibility = View.GONE
            }
            is PluginState.FreeTrial -> {
                if (state.isValid) {
                    view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_free_trial_body)
                    view.accountDialogCard.topButton.text = getString(R.string.settings_header_registered_add_button)
                    view.accountDialogCard.topButton.visibility = View.VISIBLE
                    view.accountDialogCard.topButton.setOnClickListener {
                        //TODO take to Google Play Billing add flow
                    }
                    view.accountDialogCard.textLabel.text = "Free trial: ${state.remainingDays}d"
                    view.accountDialogCard.textLabel.visibility = View.VISIBLE
                } else {
                    view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_free_trial_expired_body)
                    view.accountDialogCard.topButton.text = getString(R.string.settings_header_registered_add_button)
                    view.accountDialogCard.topButton.visibility = View.VISIBLE
                    view.accountDialogCard.topButton.setOnClickListener {
                        //TODO take to Google Play Billing add flow
                    }
                    view.accountDialogCard.textLabel.text = "Free trial expired"
                    view.accountDialogCard.textLabel.visibility = View.VISIBLE
                }
            }
            is PluginState.Subscribed -> {
                if (state.isValid) {
                    view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_subscribed_body)
                    view.accountDialogCard.topButton.visibility = View.GONE
                    view.accountDialogCard.textLabel.visibility = View.VISIBLE
                    view.accountDialogCard.textLabel.text = "Added"
                } else {
                    view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_subscribed_expired_body)
                    view.accountDialogCard.topButton.visibility = View.VISIBLE
                    view.accountDialogCard.topButton.text = "Renew"
                    view.accountDialogCard.topButton.setOnClickListener {
                        //TODO take to Google Play Billing renewal flow
                    }
                    view.accountDialogCard.textLabel.visibility = View.VISIBLE
                    view.accountDialogCard.textLabel.text = "Subscription expired"
                }

            }
        }

        view.accountDialogCard.bottomButton.visibility = View.GONE
        view.accountDialogCard.visibility = View.VISIBLE

        // Common settings
        view.signOutSetting.settingsTitle.text = getString(R.string.settings_sign_out_title)
        view.signOutSetting.settingsDescription.text = if (user.email.isNotBlank()) user.email else getString(R.string.settings_sign_out_default_desc)
        view.signOutSetting.settingsItem.setOnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        }

        view.signOutSetting.checkbox.visibility = View.INVISIBLE
        view.signOutSetting.visibility = View.VISIBLE
    }

}
