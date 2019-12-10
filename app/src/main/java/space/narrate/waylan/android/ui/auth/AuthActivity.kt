package space.narrate.waylan.android.ui.auth

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
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.widget.ProgressUnderlineView
import space.narrate.waylan.core.util.getColorFromAttr
import space.narrate.waylan.core.util.getStringOrNull
import kotlin.coroutines.CoroutineContext

class AuthActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var containerLayout: LinearLayout
    private lateinit var credentialsContainerLayout: LinearLayout
    private lateinit var editTextContainerLayout: LinearLayout
    private lateinit var emailEditText: AppCompatEditText
    private lateinit var passwordEditText: AppCompatEditText
    private lateinit var confirmPasswordEditText: AppCompatEditText
    private lateinit var doneButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var altCredentialTypeButton: MaterialButton
    private lateinit var errorTextView: AppCompatTextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarTop: ProgressUnderlineView

    private val navigator: Navigator by inject()

    private val authViewModel: AuthViewModel by viewModel()

    // A property to hold an intent which should be passed through and handled by the next
    // activity. ie. A ACTION_PROCESS_TEXT extra that should be handled by MainActivity
    // but has landed here while the user is authorized.
    private var filterIntent: Intent? = null

    private var lastErrorStateIsShown = false

    // A textRes watcher that ensures the textRes area's error state is not shown if the user
    // is typing. This results in an error message that shows on error, but disappears as
    // soon as the user begins correcting (instead of a timeout or other logic)
    private val errorMessageTextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            authViewModel.onAnyTextChanged()
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

        containerLayout = findViewById(R.id.container)
        credentialsContainerLayout = findViewById(R.id.credentials_container)
        editTextContainerLayout = findViewById(R.id.edit_text_container)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        doneButton = findViewById(R.id.done_button)
        cancelButton = findViewById(R.id.cancel_button)
        altCredentialTypeButton = findViewById(R.id.alternate_credintial_type_button)
        errorTextView = findViewById(R.id.error_text_view)
        progressBar = findViewById(R.id.progress_bar)
        progressBarTop = findViewById(R.id.progress_bar_top)
        progressBarTop.startProgress()

        ViewCompat.setOnApplyWindowInsetsListener(containerLayout) { _, insets ->
            handleApplyWindowInsets(insets)
        }

        handleIntent(intent)

        emailEditText.addTextChangedListener(errorMessageTextWatcher)
        passwordEditText.addTextChangedListener(errorMessageTextWatcher)
        confirmPasswordEditText.addTextChangedListener(errorMessageTextWatcher)

        cancelButton.setOnClickListener { authViewModel.onCancelClicked() }

        authViewModel.nightMode.observe(this) {
            delegate.localNightMode = it.value
        }

        authViewModel.shouldShowError.observe(this) { event ->
            event.getUnhandledContent()?.let { model ->
                when (model) {
                    is ShowErrorModel.Error -> {
                        val message = getStringOrNull(model.messageRes) ?: model.message
                        showErrorMessage(message)
                    }
                    else -> hideErrorMessage()
                }
            }
        }

        authViewModel.showLoading.observe(this) {
            showLoading(it)
        }

        authViewModel.authRoute.observe(this) { route ->
            when (route) {
                AuthRoute.LOG_IN -> setToLoginUi()
                AuthRoute.SIGN_UP -> setToSignUpUi()
                AuthRoute.ANONYMOUS -> setToAuthenticateUI()
            }
        }

        authViewModel.shouldShowCredentials.observe(this) { event ->
            event?.getUnhandledContent()?.let {
                showCredentialsUi(it.delayMillis)
            }
        }

        authViewModel.shouldLaunchMain.observe(this) { event ->
            event?.getUnhandledContent()?.let {
                navigator.toHome(this@AuthActivity, true, filterIntent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }

        // Get the intended AuthRoute and configure accordingly
        val authRoute = intent.getStringExtra(AUTH_ROUTE_EXTRA_KEY)

        authViewModel.onAuthRouteReceived(AuthRoute.fromName(authRoute))
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

    private fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        containerLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight,
            insets.systemWindowInsetBottom
        )

        return insets
    }

    // Alter the UI to allow login
    private fun setToLoginUi() {
        TransitionManager.beginDelayedTransition(containerLayout)
        confirmPasswordEditText.visibility = View.GONE
        altCredentialTypeButton.text = getString(R.string.auth_sign_up_button)
        doneButton.text = getString(R.string.auth_log_in_button)
        doneButton.setOnClickListener {
            authViewModel.onLoginClicked(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
        altCredentialTypeButton.setOnClickListener {
            authViewModel.onSignUpAlternateClicked()
        }
    }

    // Alter the UI to allow sign up
    private fun setToSignUpUi() {
        TransitionManager.beginDelayedTransition(containerLayout)
        confirmPasswordEditText.visibility = View.VISIBLE
        altCredentialTypeButton.text = getString(R.string.auth_log_in_button)
        doneButton.text = getString(R.string.auth_sign_up_button)
        doneButton.setOnClickListener {
            authViewModel.onSignUpClicked(
                emailEditText.text.toString(),
                passwordEditText.text.toString(),
                confirmPasswordEditText.text.toString()
            )
        }
        altCredentialTypeButton.setOnClickListener {
            authViewModel.onLoginAlternateClicked()
        }
    }

    private fun setToAuthenticateUI() {
        progressBarTop.startProgress()
        val anim = ObjectAnimator.ofFloat(
            progressBarTop,
            "alpha",
            1F
        )
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) { }
            override fun onAnimationEnd(animation: Animator?) { }
            override fun onAnimationCancel(animation: Animator?) { }
            override fun onAnimationStart(animation: Animator?) {
                progressBarTop.visibility = View.VISIBLE
            }
        })
        anim.interpolator = FastOutSlowInInterpolator()
        anim.duration = 300
        anim.startDelay = 3000
        anim.start()
    }

    // Transition from the splash screen to the credentials layout
    private fun showCredentialsUi(delayMillis: Long) = launch {
        delay(delayMillis)

        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(
                containerLayout,
                "translationY",
                resources.displayMetrics.density * -100
            ),
            ObjectAnimator.ofFloat(
                credentialsContainerLayout,
                "alpha",
                0F,
                1F
            ),
            ObjectAnimator.ofFloat(
                progressBarTop,
                "alpha",
                0F
            )
        )
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                progressBarTop.visibility = View.INVISIBLE
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {
                credentialsContainerLayout.visibility = View.VISIBLE
            }
        })
        set.interpolator = FastOutSlowInInterpolator()
        set.duration = 300
        set.start()
    }

    private fun showErrorMessage(message: String) {
        synchronized(lastErrorStateIsShown) {
            if (!lastErrorStateIsShown) {
                lastErrorStateIsShown = true

                errorTextView.text = message
                errorTextView.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_enter)
                        .apply {
                            interpolator = FastOutSlowInInterpolator()
                            setTarget(errorTextView)
                            start()
                        }
                val bgTransition = editTextContainerLayout.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.startTransition(200)

                val errorTextColor = ContextCompat.getColor(
                    this,
                    R.color.on_error_emphasis_high_type
                )
                val errorHintColor = ContextCompat.getColor(
                    this,
                    R.color.on_error_emphasis_disabled
                )
                TransitionManager.beginDelayedTransition(containerLayout)
                emailEditText.setTextColor(errorTextColor)
                emailEditText.setHintTextColor(errorHintColor)
                passwordEditText.setTextColor(errorTextColor)
                passwordEditText.setHintTextColor(errorHintColor)
                confirmPasswordEditText.setTextColor(errorTextColor)
                confirmPasswordEditText.setHintTextColor(errorHintColor)

            }
        }
    }

    private fun hideErrorMessage() {
        if (lastErrorStateIsShown) {
            synchronized(lastErrorStateIsShown) {
                lastErrorStateIsShown = false
                errorTextView.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_exit)
                        .apply {
                            interpolator = FastOutLinearInInterpolator()
                            setTarget(errorTextView)
                            start()
                        }
                val bgTransition = editTextContainerLayout.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.reverseTransition(200)

                val textColor = getColorFromAttr(R.attr.colorOnBackground)
                val hintColor = getColorFromAttr(R.attr.colorOnBackground)
                TransitionManager.beginDelayedTransition(containerLayout)
                emailEditText.setTextColor(textColor)
                emailEditText.setHintTextColor(hintColor)
                passwordEditText.setTextColor(textColor)
                passwordEditText.setHintTextColor(hintColor)
                confirmPasswordEditText.setTextColor(textColor)
                confirmPasswordEditText.setHintTextColor(hintColor)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.INVISIBLE

        altCredentialTypeButton.isEnabled = !show
        doneButton.isEnabled = !show
        cancelButton.isEnabled = !show

        altCredentialTypeButton.isClickable = !show
        doneButton.isClickable = !show
        cancelButton.isClickable = !show
    }

    companion object {
        const val AUTH_ROUTE_EXTRA_KEY = "auth_route_extra_key"
    }
}
