package com.words.android.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.words.android.R
import com.words.android.data.firestore.users.User
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.common.BaseUserActivity
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*

class SettingsActivity : BaseUserActivity() {

    val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        navigationIcon.setOnClickListener { onBackPressed() }


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
        darkModeSettings.settingsTitle.text = "Dark mode"
        darkModeSettings.settingsDescription.text = "Reading late at night? Keep looking up words and give your eyes a break."
        darkModeSettings.settingsItem.setOnClickListener {
            toggleCheckbox(darkModeSettings.checkbox)
            //TODO toggle theme
        }

        aboutSetting.settingsTitle.text = "About"
        aboutSetting.settingsDescription.text = "Curious about where Words came from?"
        aboutSetting.checkbox.visibility = View.INVISIBLE
        aboutSetting.setOnClickListener {
            //TODO open an about page
        }

        contactSetting.settingsTitle.text = "Contact"
        contactSetting.settingsDescription.text = "Get in touch."
        contactSetting.checkbox.visibility = View.INVISIBLE
        contactSetting.setOnClickListener {
            //TODO send an email share intent
        }
    }

    private fun setAsAnonymous() {
        accountDialogCard.messageTextView.text = "Want to save your recent and favorite words? Create an account and keep building your vocabulary or log in to restore your history."
        accountDialogCard.topButton.text = "Create Account"
        accountDialogCard.bottomButton.text = "Log in"
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
        accountDialogCard.messageTextView.text = "Merriam-Webster’s dictionary, Words community dictionary and multi-device login are available via a Words subscription."
        accountDialogCard.topButton.text = "Add"
        accountDialogCard.topButton.setOnClickListener {
            //TODO launch Google Play billing flow
        }

        accountDialogCard.topButton.visibility = View.VISIBLE
        accountDialogCard.bottomButton.visibility = View.GONE
        accountDialogCard.visibility = View.VISIBLE
    }

    private fun setAsHasPurchasedMerriamWebster(user: User) {
        accountDialogCard.messageTextView.text = "Thanks for your support! We hope your enjoying Words, Merriam-Webster definitions and the community."
        accountDialogCard.topButton.text = "Support"
        accountDialogCard.bottomButton.text = "Dismiss"

        accountDialogCard.topButton.visibility = View.VISIBLE
        accountDialogCard.bottomButton.visibility = View.VISIBLE
        accountDialogCard.visibility = View.VISIBLE
    }

    private fun setRegisteredUserCommonSettings(user: User) {
        signOutSetting.settingsTitle.text = "Sign out"
        signOutSetting.settingsDescription.text = user.firebaseUser?.email ?: "Sign out and log in with a different account."

        signOutSetting.checkbox.visibility = View.INVISIBLE
        signOutSetting.visibility = View.VISIBLE
    }

    private fun launchAuth(authRoute: AuthActivity.AuthRoute) {
        val intent = Intent(this, AuthActivity::class.java)
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
