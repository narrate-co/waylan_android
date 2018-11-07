package com.words.android.ui.auth

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.words.android.App
import com.words.android.R
import com.words.android.MainActivity
import com.words.android.Navigator
import com.words.android.util.FirebaseAuthWordException
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    var filterIntent: Intent? = null

    //TODO clean up
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        handleIntent(intent)

        val auth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = auth.currentUser

        if (firebaseUser != null) {
            cancel.setOnClickListener {
                returnMain(firebaseUser)
            }
        }

        email.addTextChangedListener(errorMessageTextWatcher)
        password.addTextChangedListener(errorMessageTextWatcher)
        confirmPassword.addTextChangedListener(errorMessageTextWatcher)

        authViewModel.getIsLoading().observe(this, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE

            alternateCredentialType.isEnabled = !it
            done.isEnabled = !it
            cancel.isEnabled = !it

            alternateCredentialType.isClickable = !it
            done.isClickable = !it
            cancel.isClickable = !it
        })

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
                    returnMain(firebaseUser)
                } else {
                    launch(UI) {
                        try {
                            val user = authViewModel.signUpAnonymously()
                            launchMain(user, true)
                        } catch (e: Exception) {
                            showErrorMessage(e)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && filterIntent == null) {
            filterIntent = intent
        }
    }

    private fun returnMain(firebaseUser: FirebaseUser) {
        launch(UI) {
            try {
                val auth = authViewModel.getCurrentAuth(firebaseUser)
                launchMain(auth, true)
            } catch (e: Exception) {
                showErrorMessage(e)
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
                    launchMain(loggedInUser, true)
                } catch (e: Exception) {
                    showErrorMessage(e)
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
                    launchMain(newlyLinkedUser, true)
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
        //TODO clean this up?
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

    private fun launchMain(auth: Auth?, clearStack: Boolean, delayMillis: Long = 0L) {
        (application as App).setUser(auth)
        launch(UI) {
            delay(delayMillis, TimeUnit.MILLISECONDS)

            Navigator.launchMain(this@AuthActivity, clearStack, filterIntent)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
