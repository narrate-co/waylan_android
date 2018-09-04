package com.words.android.ui.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.words.android.R
import com.words.android.ui.auth.AuthActivity
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_card_view_layout.*
import kotlinx.android.synthetic.main.dialog_card_view_layout.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //TODO dynamically add settings
        //TODO create custom setting item view
        navigationIcon.setOnClickListener { onBackPressed() }

        pushNotificationsSetting.settingsTitle.text = "Push notifications"
        pushNotificationsSetting.settingsDescription.text = "Enable notifications to remind you of words you’ve recently looked up"

        signOutSetting.settingsTitle.text = "Sign out"
        signOutSetting.settingsDescription.text = "Exit and log in with a different account"
        signOutSetting.checkbox.visibility = View.GONE
        signOutSetting.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        accountDialogCard.topButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.SIGN_UP)
        }
        accountDialogCard.bottomButton.setOnClickListener {
            launchAuth(AuthActivity.AuthRoute.LOG_IN)
        }

        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == false) {
            //There's a user who is NOT Anonymous
            //TODO remove accountDialogCard
            accountDialogCard.visibility = View.GONE
            //TODO add setting to log out
            signOutSetting.settingsDescription.text = FirebaseAuth.getInstance().currentUser?.email ?: "Exit and log in with a different account"
            signOutSetting.visibility = View.VISIBLE
        }

    }


    private fun launchAuth(authRoute: AuthActivity.AuthRoute) {
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        startActivity(intent)
    }
}
