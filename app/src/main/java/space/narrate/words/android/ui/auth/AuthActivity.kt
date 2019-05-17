package space.narrate.words.android.ui.auth

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.google.firebase.auth.*
import space.narrate.words.android.App
import space.narrate.words.android.R
import space.narrate.words.android.Navigator
import space.narrate.words.android.util.getColorFromAttr
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * An Activity which acts as a splash screen, an [Auth] getter/setter and a Login or Sign up
 * screen.
 */
class AuthActivity : DaggerAppCompatActivity(), CoroutineScope {

    companion object {
        const val TAG = "AuthActivity"
        const val AUTH_ROUTE_EXTRA_KEY = "auth_route_extra_key"
    }

    /**
     * An enumeration that defines how this activity should behave. Use the key
     * [AUTH_ROUTE_EXTRA_KEY] and add an [AuthRoute] to [AuthActivity]'s start intent to
     * determine what [AuthActivity] should expect to accomplish.
     *
     * For example, using [SIGN_UP] will force [AuthActivity] to immediately display the
     * credentials input layout along with both a password and a confirm password field.
     *
     * Note: To allow either log in or sign up in a single layout, a user can switch between
     * the two routes.
     */
    enum class AuthRoute {

        /**
         * An [AuthRoute] which checks for a current [FirebaseUser] or creates a new
         * anonymous [FirebaseUser].
         *
         * If a [FirebaseUser] is present, the [space.narrate.words.android.data.firestore.users.User]
         * is retreived, [App.setUser] is called and this activity finishes. If there is not a
         * FirebaseUser, a new anonymous user is created, a new
         * [space.narrate.words.android.data.firestore.users.User] is created, [App.setUser] is called
         * and this activity finishes.
         *
         * No UI is shown during this route other than the logo
         *
         * TODO handle a poor/no network connection state
         */
        ANONYMOUS,

        /**
         * An [AuthRoute] that allows a user create a new account or tie their
         * existing anonymous account to an email and password.
         *
         * Once credentials are entered, they are checked an either tied to an existing anonymous
         * user or a new user is created. The [space.narrate.words.android.data.firestore.users.User]
         * for the [FirebaseUser] is then either updated or created, [App.setUser] is called and
         * this activity finishes.
         */
        SIGN_UP,

        /**
         * An [AuthRoute] that allows a user log in with existing email/password
         * credentials.
         *
         * Once credentials are entered, the user is logged in via [FirebaseAuth], their
         * [space.narrate.words.android.data.firestore.users.User] is retreived, [App.setUser] is called
         * and this activity finishes.
         */
        LOG_IN
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val authViewModel by lazy {
        ViewModelProviders.of(this).get(AuthViewModel::class.java)
    }

    // A property to hold an intent which should be passed through and handled by the next
    // activity. ie. A ACTION_PROCESS_TEXT extra that should be handled by MainActivity
    // but has landed here while the user is authorized.
    private var filterIntent: Intent? = null

    // A textRes watcher that ensures the textRes area's error state is not shown if the user
    // is typing. This results in an error message that shows on error, but disappears as
    // soon as the user begins correcting (instead of a timeout or other logic)
    private val errorMessageTextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            hideErrorMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val decor = window.decorView
        val flags = decor.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        decor.systemUiVisibility = flags

        handleIntent(intent)

        initViews()

        // Get the intended AuthRoute and configure accordingly
        val authRoute = intent.getStringExtra(AUTH_ROUTE_EXTRA_KEY)
        initAuthState(authRoute)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // Catch any intents that should be passed through after auth has happened
    private fun handleIntent(intent: Intent?) {
        if (intent != null && filterIntent == null) {
            filterIntent = intent
        }
    }

    // Initialize common views across all routes and configurations
    private fun initViews() {
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
    }

    // Initialize and act according to the given authRoute. See AuthRoute for more details
    private fun initAuthState(authRoute: String?) {
        // Get the current FirebaseUser
        val auth = FirebaseAuth.getInstance()
        val firebaseUser: FirebaseUser? = auth.currentUser

        // If the user is not null, allow them to hit cancel to return to an authorized session
        // TODO properly log a user out and remove this functionality
        if (firebaseUser != null) {
            cancel.setOnClickListener {
                getCurrentAuthAndReturnToMain(firebaseUser)
            }
        }

        // Check AuthRoute to determine intended behavior
        when (authRoute) {
            // Sign up and log in AuthRoute
            AuthRoute.SIGN_UP.name, AuthRoute.LOG_IN.name -> {
                launch {
                    delay(500)
                    if (authRoute == AuthRoute.LOG_IN.name) setToLoginUi() else setToSignUpUi()
                    showCredentialsUi()
                }
            }
            // Anonymous or null authRoute
            else -> {
                if (firebaseUser != null ) {
                    getCurrentAuthAndReturnToMain(firebaseUser)
                } else {
                    launch {
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


    // Alter the UI to allow login
    private fun setToLoginUi() {
        TransitionManager.beginDelayedTransition(root)
        confirmPassword.visibility = View.GONE
        alternateCredentialType.text = getString(R.string.auth_sign_up_button)
        done.text = getString(R.string.auth_log_in_button)
        done.setOnClickListener {
            launch {
                try {
                    val loggedInUser = authViewModel.logIn(
                            email.text.toString(),
                            password.text.toString()
                    )
                    launchMain(loggedInUser, true)
                } catch (e: Exception) {
                    showErrorMessage(e)
                }
            }
        }
        alternateCredentialType.setOnClickListener { setToSignUpUi() }
    }

    // Alter the UI to allow sign up
    private fun setToSignUpUi() {
        TransitionManager.beginDelayedTransition(root)
        confirmPassword.visibility = View.VISIBLE
        alternateCredentialType.text = getString(R.string.auth_log_in_button)
        done.text = getString(R.string.auth_sign_up_button)
        done.setOnClickListener {
            launch {
                try {
                    val newlyLinkedUser = authViewModel.signUp(
                            email.text.toString(),
                            password.text.toString(),
                            confirmPassword.text.toString()
                    )
                    launchMain(newlyLinkedUser, true)
                } catch (e: Exception) {
                    showErrorMessage(e)
                }
            }
        }
        alternateCredentialType.setOnClickListener { setToLoginUi() }

    }

    // Transition from the splash screen to the credentials layout
    private fun showCredentialsUi() {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(
                        container,
                        "translationY",
                        resources.displayMetrics.density * -100
                ),
                ObjectAnimator.ofFloat(
                        credentialsContainer,
                        "alpha",
                        0F,
                        1F
                )
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

        synchronized(lastErrorStateIsShown) {
            if (!lastErrorStateIsShown) {
                lastErrorStateIsShown = true

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

                val errorTextColor = getColorFromAttr(R.attr.colorOnError)
                val errorHintColor = getColorFromAttr(R.attr.colorOnError)
                TransitionManager.beginDelayedTransition(container)
                email.setTextColor(errorTextColor)
                email.setHintTextColor(errorHintColor)
                password.setTextColor(errorTextColor)
                password.setHintTextColor(errorHintColor)
                confirmPassword.setTextColor(errorTextColor)
                confirmPassword.setHintTextColor(errorHintColor)

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

                val textColor = getColorFromAttr(R.attr.colorOnBackground)
                val hintColor = getColorFromAttr(R.attr.colorOnBackground)
                TransitionManager.beginDelayedTransition(container)
                email.setTextColor(textColor)
                email.setHintTextColor(hintColor)
                password.setTextColor(textColor)
                password.setHintTextColor(hintColor)
                confirmPassword.setTextColor(textColor)
                confirmPassword.setHintTextColor(hintColor)
            }
        }
    }

    // A helper function to create an Auth object and then call launchMain
    private fun getCurrentAuthAndReturnToMain(firebaseUser: FirebaseUser) {
        launch {
            try {
                val auth = authViewModel.getCurrentAuth(firebaseUser)
                launchMain(auth, true)
            } catch (e: Exception) {
                showErrorMessage(e)
            }
        }
    }

    // Set the user and go to MainActivity
    private fun launchMain(auth: Auth?, clearStack: Boolean, delayMillis: Long = 0L) {
        (application as App).setUser(auth)
        launch {
            delay(delayMillis)

            Navigator.launchMain(this@AuthActivity, clearStack, filterIntent)

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
