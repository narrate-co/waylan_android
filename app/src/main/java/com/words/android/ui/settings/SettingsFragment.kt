package com.words.android.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.words.android.*
import com.words.android.data.firestore.users.User
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.settings_fragment.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*
import com.words.android.data.firestore.users.PluginState
import com.words.android.util.setChecked


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
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        view.navigationIcon.setOnClickListener { activity?.onBackPressed() }
        setSettings(view, viewModel.getUser())
        return view
    }

    private fun setSettings(view: View, user: User?) {
        //set state specific settings
        when {
            user == null -> setAsAnonymous(view, PluginState.NONE)
            user.isAnonymous -> setAsAnonymous(view, user.merriamWebsterState)
            else -> setAsRegistered(view, user)
        }

        //set common settings
        view.darkModeSettings.settingsTitle.text = getString(R.string.settings_dark_mode_title)
        view.darkModeSettings.settingsDescription.text = getString(R.string.settings_dark_mode_desc)
        val usesDarkMode = viewModel.usesDarkMode ?: false
        view.darkModeSettings.checkbox.setChecked(usesDarkMode)
        view.darkModeSettings.settingsItem.setOnClickListener {
            val currentValue = viewModel.usesDarkMode
            view.darkModeSettings.checkbox.setChecked(!currentValue)
            viewModel.usesDarkMode = !currentValue
            (activity as? SettingsActivity)?.restartWithReconstructedStack()
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

    private fun setAsAnonymous(view: View, merriamWebsterState: PluginState) {
        when (merriamWebsterState) {
            PluginState.FREE_TRIAL -> {
                view.accountDialogCard.textLabel.text = "Free trial: 7d"
                view.accountDialogCard.textLabel.visibility = View.VISIBLE
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_anonymous_free_trial_body)
            }
            else -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_anonymous_none_body)
                view.accountDialogCard.textLabel.visibility = View.GONE
            }
        }
        view.accountDialogCard.topButton.text = getString(R.string.settings_header_anonymous_create_account_button)
        view.accountDialogCard.bottomButton.text = getString(R.string.settings_header_anonymous_log_in_button)
        view.accountDialogCard.topButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.SIGN_UP)
        }
        view.accountDialogCard.bottomButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        view.accountDialogCard.topButton.visibility = View.VISIBLE
        view.accountDialogCard.bottomButton.visibility = View.VISIBLE
        view.accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsRegistered(view: View, user: User) {
        when (user.merriamWebsterState) {
            PluginState.NONE -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_none_body)
                view.accountDialogCard.topButton.text = getString(R.string.settings_header_registered_add_button)
                view.accountDialogCard.topButton.visibility = View.VISIBLE
                view.accountDialogCard.textLabel.visibility = View.GONE
            }
            PluginState.FREE_TRIAL -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_free_trial_body)
                view.accountDialogCard.topButton.text = getString(R.string.settings_header_registered_add_button)
                view.accountDialogCard.topButton.visibility = View.VISIBLE
                view.accountDialogCard.textLabel.visibility = View.VISIBLE
                view.accountDialogCard.textLabel.text = "Free trial: 30d"
            }
            PluginState.PURCHASED -> {
                view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_registered_purchased_body)
                view.accountDialogCard.topButton.visibility = View.GONE
                view.accountDialogCard.textLabel.visibility = View.VISIBLE
                view.accountDialogCard.textLabel.text = "Added"
            }
        }

        view.accountDialogCard.bottomButton.visibility = View.GONE
        view.accountDialogCard.visibility = View.VISIBLE

        // Common settings
        view.signOutSetting.settingsTitle.text = getString(R.string.settings_sign_out_title)
        view.signOutSetting.settingsDescription.text = if (user.email.isNotBlank()) user.email else getString(R.string.settings_sign_out_default_desc)
        view.signOutSetting.settingsItem.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        view.signOutSetting.checkbox.visibility = View.INVISIBLE
        view.signOutSetting.visibility = View.VISIBLE
    }

    private fun launchAuth(authRoute: AuthActivity.AuthRoute) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        startActivity(intent)
    }
}
