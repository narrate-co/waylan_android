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
import com.words.android.App
import com.words.android.Config
import com.words.android.Navigator
import com.words.android.R
import com.words.android.data.firestore.users.User
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.settings_fragment.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*
import androidx.core.content.IntentCompat
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import androidx.core.app.TaskStackBuilder


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

    private val preferenceRepository by lazy { (activity?.application as? App)?.preferenceRepository }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        view.navigationIcon.setOnClickListener { activity?.onBackPressed() }
        setSettings(view, viewModel.getUser())
        return view
    }

    override fun onEnterTransactionEnded() {
    }

    private fun setSettings(view: View, user: User?) {
        //set state specific settings
        if (user == null || user.firebaseUser?.isAnonymous == true) {
            //set to anonymous settings
            setAsAnonymous(view)
        } else if (!user.isMerriamWebsterSubscriber) {
            //set to registered mw offer settings
            setAsCanPurchaseMerriamWebster(view, user)
            setRegisteredUserCommonSettings(view, user)
        } else {
            //set to registered mw purchased settings
            setAsHasPurchasedMerriamWebster(view, user)
            setRegisteredUserCommonSettings(view, user)
        }


        //set common settings
        view.darkModeSettings.settingsTitle.text = getString(R.string.settings_dark_mode_title)
        view.darkModeSettings.settingsDescription.text = getString(R.string.settings_dark_mode_desc)
        val usesDarkMode = preferenceRepository?.usesDarkMode ?: false
        setCheckbox(usesDarkMode, view.darkModeSettings.checkbox)
        view.darkModeSettings.settingsItem.setOnClickListener {
            val currentValue = preferenceRepository?.usesDarkMode ?: false
            setCheckbox(!currentValue, view.darkModeSettings.checkbox)
            preferenceRepository?.usesDarkMode = !currentValue
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
    }

    private fun setAsAnonymous(view: View) {
        view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_create_account_body)
        view.accountDialogCard.topButton.text = getString(R.string.settings_header_create_account_create_account_button)
        view.accountDialogCard.bottomButton.text = getString(R.string.settings_header_create_account_log_in_button)
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

    private fun setAsCanPurchaseMerriamWebster(view: View, user: User) {

        //set account dialog card to purchase merriam webseter mode
        view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_purchase_merriam_webster_body)
        view.accountDialogCard.topButton.text = getString(R.string.settings_header_purchase_merriam_webster_add_button)
        view.accountDialogCard.topButton.setOnClickListener {
            //TODO launch Google Play billing flow
        }

        view.accountDialogCard.topButton.visibility = View.VISIBLE
        view.accountDialogCard.bottomButton.visibility = View.GONE
        view.accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsHasPurchasedMerriamWebster(view: View, user: User) {
        view.accountDialogCard.messageTextView.text = getString(R.string.settings_header_added_merriam_webster_body)

        view.accountDialogCard.topButton.visibility = View.GONE
        view.accountDialogCard.bottomButton.visibility = View.GONE
        view.accountDialogCard.visibility = View.VISIBLE
    }

    private fun setRegisteredUserCommonSettings(view: View, user: User) {
        view.signOutSetting.settingsTitle.text = getString(R.string.settings_sign_out_title)
        view.signOutSetting.settingsDescription.text = user.firebaseUser?.email ?: getString(R.string.settings_sign_out_default_desc)
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

    private fun setCheckbox(value: Boolean, imageButton: AppCompatImageButton) {
        if (value) {
            imageButton.setImageResource(R.drawable.ic_round_check_circle_outline_black_24px)
        } else {
            imageButton.setImageResource(R.drawable.ic_round_check_circle_black_24px)
        }
    }

}
