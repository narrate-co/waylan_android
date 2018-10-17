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
import com.words.android.Config
import com.words.android.Navigator
import com.words.android.R
import com.words.android.data.firestore.users.User
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.settings_fragment.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

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
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onEnterTransactionEnded() {
        setSettings(viewModel.getUser())
    }

    private fun setSettings(user: User?) {
        //set state specific settings
        if (user == null || user.firebaseUser?.isAnonymous == true) {
            //set to anonymous settings
            setAsAnonymous()
        } else if (!user.isMerriamWebsterSubscriber) {
            //set to registered mw offer settings
            setAsCanPurchaseMerriamWebster(user)
            setRegisteredUserCommonSettings(user)
        } else {
            //set to registered mw purchased settings
            setAsHasPurchasedMerriamWebster(user)
            setRegisteredUserCommonSettings(user)
        }


        //set common settings
        darkModeSettings.visibility = View.GONE
        darkModeSettings.settingsTitle.text = getString(R.string.settings_dark_mode_title)
        darkModeSettings.settingsDescription.text = getString(R.string.settings_dark_mode_desc)
        darkModeSettings.settingsItem.setOnClickListener {
            toggleCheckbox(darkModeSettings.checkbox)
            //TODO toggle theme
        }

        aboutSetting.settingsTitle.text = getString(R.string.settings_about_title)
        aboutSetting.settingsDescription.text = getString(R.string.settings_about_desc)
        aboutSetting.checkbox.visibility = View.INVISIBLE
        aboutSetting.setOnClickListener {
            (activity as? SettingsActivity)?.showAbout()
        }

        contactSetting.settingsTitle.text = getString(R.string.settings_contact_title)
        contactSetting.settingsDescription.text = getString(R.string.settings_contact_desc)
        contactSetting.checkbox.visibility = View.INVISIBLE
        contactSetting.setOnClickListener {
            try {
                Navigator.launchEmail(context!!, Config.SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(settingsRoot, getString(R.string.settings_email_compose_no_client_error), Snackbar.LENGTH_SHORT)
                        .configError(context!!, false)
                        .show()
            }
        }

        //TODO this is a terrible temporary hack while testing
        launch (UI) {
            delay(200)
            settingsContainer.visibility = View.VISIBLE
        }
    }

    private fun setAsAnonymous() {
        accountDialogCard.messageTextView.text = getString(R.string.settings_header_create_account_body)
        accountDialogCard.topButton.text = getString(R.string.settings_header_create_account_create_account_button)
        accountDialogCard.bottomButton.text = getString(R.string.settings_header_create_account_log_in_button)
        accountDialogCard.topButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.SIGN_UP)
        }
        accountDialogCard.bottomButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        accountDialogCard.topButton.visibility = View.VISIBLE
        accountDialogCard.bottomButton.visibility = View.VISIBLE
        accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsCanPurchaseMerriamWebster(user: User) {

        //set account dialog card to purchase merriam webseter mode
        accountDialogCard.messageTextView.text = getString(R.string.settings_header_purchase_merriam_webster_body)
        accountDialogCard.topButton.text = getString(R.string.settings_header_purchase_merriam_webster_add_button)
        accountDialogCard.topButton.setOnClickListener {
            //TODO launch Google Play billing flow
        }

        accountDialogCard.topButton.visibility = View.VISIBLE
        accountDialogCard.bottomButton.visibility = View.GONE
        accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsHasPurchasedMerriamWebster(user: User) {
        accountDialogCard.messageTextView.text = getString(R.string.settings_header_added_merriam_webster_body)

        accountDialogCard.topButton.visibility = View.GONE
        accountDialogCard.bottomButton.visibility = View.GONE
        accountDialogCard.visibility = View.VISIBLE
    }

    private fun setRegisteredUserCommonSettings(user: User) {
        signOutSetting.settingsTitle.text = getString(R.string.settings_sign_out_title)
        signOutSetting.settingsDescription.text = user.firebaseUser?.email ?: getString(R.string.settings_sign_out_default_desc)
        signOutSetting.settingsItem.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        signOutSetting.checkbox.visibility = View.INVISIBLE
        signOutSetting.visibility = View.VISIBLE
    }

    private fun launchAuth(authRoute: AuthActivity.AuthRoute) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        startActivity(intent)
    }

    /**
     * @return the post toggle state of the checkbox. If the checkbox was NOT checked before calling
     * toggleCheckbox, this method will return TRUE
     */
    private fun toggleCheckbox(imageButtom: AppCompatImageButton): Boolean {
        val currentDrawable = imageButtom.drawable.constantState
        val checkedDrawable = resources.getDrawable(R.drawable.ic_round_check_circle_24px).constantState

        val isChecked = currentDrawable.equals(checkedDrawable)

        if (isChecked) {
            imageButtom.setImageResource(R.drawable.ic_round_check_circle_outline_24px)
        } else {
            imageButtom.setImageResource(R.drawable.ic_round_check_circle_24px)
        }

        return !isChecked
    }

}
