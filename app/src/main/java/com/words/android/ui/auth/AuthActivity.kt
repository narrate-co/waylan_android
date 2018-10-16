package com.words.android.ui.auth

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.words.android.App
import com.words.android.R
import com.words.android.Config
import com.words.android.MainActivity
import com.words.android.data.firestore.users.User
import com.words.android.util.FirebaseAuthWordException
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

    private val errorMessageTextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            hideErrorMessage()
        }
    }

    private val authViewModel by lazy {
        ViewModelProviders.of(this).get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val auth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = auth.currentUser

        if (firebaseUser != null) {
            cancel.setOnClickListener { launchHome(firebaseUser, true) }
        }

        email.addTextChangedListener(errorMessageTextWatcher)
        password.addTextChangedListener(errorMessageTextWatcher)
        confirmPassword.addTextChangedListener(errorMessageTextWatcher)

        val authRoute = intent.getStringExtra(AUTH_ROUTE_EXTRA_KEY)
        when (authRoute) {
            AuthRoute.SIGN_UP.name, AuthRoute.LOG_IN.name -> {
                launch (UI) {
                    delay(500)
                    if (authRoute == AuthRoute.LOG_IN.name) setToLoginUi() else setToSignUpUi()
                    showCredentialsUi()
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
                    launch(UI) {
                        try {
                            val user = authViewModel.signUpAnonymously()
                            launchHome(user, true)
                        } catch (e: Exception) {
                            showErrorMessage(e)
                        }
                    }
                }
            }
        }


    }

    private fun setToLoginUi() {
        confirmPassword.visibility = View.GONE
        alternateCredentialType.text = "Sign up"
        done.text = "Log in"
        done.setOnClickListener {
            launch(UI) {
                try {
                    val loggedInUser = authViewModel.logIn(email.text.toString(), password.text.toString())
                    launchHome(loggedInUser, true)
                } catch (e: Exception) {
                    showErrorMessage(e) //TODO make sure this is a user friendly error
                }
            }
        }
        alternateCredentialType.setOnClickListener { setToSignUpUi() }
    }

    private fun setToSignUpUi() {
        confirmPassword.visibility = View.VISIBLE
        alternateCredentialType.text = "Log in"
        done.text = "Sign up"
        done.setOnClickListener {
            launch(UI) {
                try {
                    val newlyLinkedUser = authViewModel.signUp(email.text.toString(), password.text.toString(), confirmPassword.text.toString())
                    launchHome(newlyLinkedUser, true)
                } catch (e: Exception) {
                    showErrorMessage(e) //TODO make sure this is a user friendly error
                }
            }
        }
        alternateCredentialType.setOnClickListener { setToLoginUi() }
    }

    private fun showCredentialsUi() {
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

    private var lastErrorStateIsShown = false

    private fun showErrorMessage(e: Exception) {
        e.printStackTrace()
        println(e)
        synchronized(lastErrorStateIsShown) {
            if (!lastErrorStateIsShown) {
                error.text = when (e) {
                    is FirebaseAuthWordException -> getString(e.localizedMessageRes)
                    else -> e.localizedMessage
                }
                error?.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_enter)
                        .apply {
                            interpolator = FastOutSlowInInterpolator()
                            setTarget(error)
                            start()
                        }
                val bgTransition = editTextContainer.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.startTransition(200)
                lastErrorStateIsShown = true
            }
        }
    }

    private fun hideErrorMessage() {
        if (lastErrorStateIsShown) {
            synchronized(lastErrorStateIsShown) {
                lastErrorStateIsShown = false
                error?.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_exit)
                        .apply {
                            interpolator = FastOutLinearInInterpolator()
                            setTarget(error)
                            start()
                        }
                val bgTransition = editTextContainer.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.reverseTransition(200)
            }
        }
    }

    private fun launchHome(user: FirebaseUser?, clearStack: Boolean, delayMillis: Long = 0L) {
        //TODO set isMerriamWebsterSubscriber properly
        //TODO use showErrorMessage() to show any validation errors
        (application as App).setUser(User(user, Config.DEBUG_USER_IS_PREMIUM))
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
