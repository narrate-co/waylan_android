package com.words.android.ui.auth

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.words.android.App
import com.words.android.Config
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.data.firestore.User
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class AuthActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthActivity"
        const val AUTH_ROUTE_EXTRA_KEY = "auth_route_extra_key"
    }

    enum class AuthRoute {
        ANONYMOUS, SIGN_UP, LOG_IN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)



        val auth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = auth.currentUser

        if (firebaseUser != null) {
            cancel.setOnClickListener { launchHome(firebaseUser, true) }
        }

        if (true) {
            launch (UI) {
                delay(500)
                setToLoginUi()
                showCredentialsUi(auth)
            }
            return
        }


        val authRoute = intent.getStringExtra(AUTH_ROUTE_EXTRA_KEY)
        when (authRoute) {
            AuthRoute.SIGN_UP.name, AuthRoute.LOG_IN.name -> {
                launch (UI) {
                    delay(500)
                    if (authRoute == AuthRoute.LOG_IN.name) setToLoginUi() else setToSignUpUi()
                    showCredentialsUi(auth)
                }
            }
            else -> {
                if (firebaseUser != null ) {
                    Log.d(TAG, "User already signed in. Entering with user uid ${firebaseUser.uid}")
                    launch(UI) {
                        delay(1500)
                        launchHome(firebaseUser, true)
                    }
                } else {
                    signInAnonymously(auth)
                }
            }
        }


    }

    private fun setToLoginUi() {
        confirmPassword.visibility = View.GONE
        alternateCredentialType.text = "Sign up"
        done.setOnClickListener { logIn() }
        alternateCredentialType.setOnClickListener { setToSignUpUi() }
    }

    private fun setToSignUpUi() {
        confirmPassword.visibility = View.VISIBLE
        alternateCredentialType.text = "Log in"
        done.setOnClickListener { signUp() }
        alternateCredentialType.setOnClickListener { setToLoginUi() }
    }

    private fun showCredentialsUi(auth: FirebaseAuth) {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(container, "translationY", resources.displayMetrics.density * -100),
                ObjectAnimator.ofFloat(credentialsContainer, "alpha", 0F, 1F)
        )
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {
                credentialsContainer.visibility = View.VISIBLE
            }
        })
        set.interpolator = FastOutSlowInInterpolator()
        set.duration = 300
        set.start()
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

    private fun logIn() {
        //TODO
    }

    private fun signUp() {
        //TODO
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
