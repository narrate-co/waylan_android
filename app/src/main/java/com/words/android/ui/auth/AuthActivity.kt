package com.words.android.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.words.android.App
import com.words.android.Config
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.data.firestore.User
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val auth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = auth.currentUser
        if (firebaseUser != null) {
            Log.d(TAG, "User already signed in. Entering with user uid ${firebaseUser.uid}")
            launch(UI) {
                delay(1500)
                launchHome(firebaseUser, true)
            }
        } else {
            signInAnonymously(auth)
        }

    }

    private fun signInAnonymously(auth: FirebaseAuth) {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //launch home
                val firebaseUser = auth.currentUser
                Log.d(TAG, "Signed in user anonymously. Entering with user uid ${firebaseUser?.uid}...")
                launchHome(firebaseUser, true)
            } else {
                Log.d(TAG, "Unable to sign in user anonymously. Entering guest mode...")
                launchHome(null, true)
            }
        }
    }


    private fun launchHome(user: FirebaseUser?, clearStack: Boolean, delayMillis: Long = 0L) {
        //TODO set isMerriamWebsterSubscriber properly
        (application as App).user = User(user, Config.DEBUG_USER_IS_PREMIUM)
        launch(UI) {
            delay(delayMillis, TimeUnit.MILLISECONDS)

            val intent = Intent(this@AuthActivity, MainActivity::class.java)
            if (clearStack) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }
}
