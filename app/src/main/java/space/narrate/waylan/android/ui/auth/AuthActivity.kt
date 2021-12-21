package space.narrate.waylan.android.ui.auth

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.google.android.material.elevation.SurfaceColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.ActivityAuthBinding
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.contentView
import space.narrate.waylan.core.util.themeColor
import space.narrate.waylan.core.util.getStringOrNull
import kotlin.coroutines.CoroutineContext
import space.narrate.waylan.core.util.windowTintElevation

class AuthActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val binding: ActivityAuthBinding by contentView(R.layout.activity_auth)

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
        windowTintElevation(R.dimen.plane_01)
        super.onCreate(savedInstanceState)
        binding.progressBarTop.startProgress()

        handleIntent(intent)

        binding.emailEditText.addTextChangedListener(errorMessageTextWatcher)
        binding.passwordEditText.addTextChangedListener(errorMessageTextWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(errorMessageTextWatcher)

        binding.cancelButton.setOnClickListener { authViewModel.onCancelClicked() }

        authViewModel.nightMode.observe(this) {
            delegate.localNightMode = it.value
        }

        authViewModel.shouldShowError.observe(this) { event ->
            event.withUnhandledContent { model ->
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
            event.withUnhandledContent {
                showCredentialsUi(it.delayMillis)
            }
        }

        authViewModel.shouldLaunchMain.observe(this) { event ->
            event.withUnhandledContent {
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

    // Alter the UI to allow login
    private fun setToLoginUi() {
        binding.run {
            TransitionManager.beginDelayedTransition(container)
            confirmPasswordEditText.visibility = View.GONE
            alternateCredintialTypeButton.text = getString(R.string.auth_sign_up_button)
            doneButton.text = getString(R.string.auth_log_in_button)
            doneButton.setOnClickListener {
                authViewModel.onLoginClicked(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                )
            }
            alternateCredintialTypeButton.setOnClickListener {
                authViewModel.onSignUpAlternateClicked()
            }
        }
    }

    // Alter the UI to allow sign up
    private fun setToSignUpUi() {
        binding.run {
            TransitionManager.beginDelayedTransition(container)
            confirmPasswordEditText.visibility = View.VISIBLE
            alternateCredintialTypeButton.text = getString(R.string.auth_log_in_button)
            doneButton.text = getString(R.string.auth_sign_up_button)
            doneButton.setOnClickListener {
                authViewModel.onSignUpClicked(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString()
                )
            }
            alternateCredintialTypeButton.setOnClickListener {
                authViewModel.onLoginAlternateClicked()
            }
        }
    }

    private fun setToAuthenticateUI() {
        binding.progressBarTop.startProgress()
        val anim = ObjectAnimator.ofFloat(
            binding.progressBarTop,
            "alpha",
            1F
        )
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) { }
            override fun onAnimationEnd(animation: Animator?) { }
            override fun onAnimationCancel(animation: Animator?) { }
            override fun onAnimationStart(animation: Animator?) {
                binding.progressBarTop.visibility = View.VISIBLE
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
                binding.container,
                "translationY",
                resources.displayMetrics.density * -100
            ),
            ObjectAnimator.ofFloat(
                binding.credentialsContainer,
                "alpha",
                0F,
                1F
            ),
            ObjectAnimator.ofFloat(
                binding.progressBarTop,
                "alpha",
                0F
            )
        )
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                binding.progressBarTop.visibility = View.INVISIBLE
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {
                binding.credentialsContainer.visibility = View.VISIBLE
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

                binding.errorTextView.text = message
                binding.errorTextView.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_enter)
                        .apply {
                            interpolator = FastOutSlowInInterpolator()
                            setTarget(binding.errorTextView)
                            start()
                        }
                val bgTransition = binding.editTextContainer.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.startTransition(200)

                val errorTextColor = ContextCompat.getColor(
                    this,
                    R.color.on_error_area_emphasis_high_type
                )
                val errorHintColor = ContextCompat.getColor(
                    this,
                    R.color.on_error_area_emphasis_disabled
                )
                binding.run {
                    TransitionManager.beginDelayedTransition(container)
                    emailEditText.setTextColor(errorTextColor)
                    emailEditText.setHintTextColor(errorHintColor)
                    passwordEditText.setTextColor(errorTextColor)
                    passwordEditText.setHintTextColor(errorHintColor)
                    confirmPasswordEditText.setTextColor(errorTextColor)
                    confirmPasswordEditText.setHintTextColor(errorHintColor)
                }
            }
        }
    }

    private fun hideErrorMessage() {
        if (lastErrorStateIsShown) {
            synchronized(lastErrorStateIsShown) {
                lastErrorStateIsShown = false
                binding.errorTextView.animation?.cancel()
                AnimatorInflater.loadAnimator(this, R.animator.error_text_exit)
                        .apply {
                            interpolator = FastOutLinearInInterpolator()
                            setTarget(binding.errorTextView)
                            start()
                        }
                val bgTransition = binding.editTextContainer.background as TransitionDrawable
                bgTransition.isCrossFadeEnabled = true
                bgTransition.reverseTransition(200)

                val textColor = themeColor(R.attr.colorOnBackground)
                val hintColor = themeColor(R.attr.colorOnBackground)

                binding.run {
                    TransitionManager.beginDelayedTransition(binding.container)
                    emailEditText.setTextColor(textColor)
                    emailEditText.setHintTextColor(hintColor)
                    passwordEditText.setTextColor(textColor)
                    passwordEditText.setHintTextColor(hintColor)
                    confirmPasswordEditText.setTextColor(textColor)
                    confirmPasswordEditText.setHintTextColor(hintColor)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.run {
            progressBar.visibility = if (show) View.VISIBLE else View.INVISIBLE

            alternateCredintialTypeButton.isEnabled = !show
            doneButton.isEnabled = !show
            cancelButton.isEnabled = !show

            alternateCredintialTypeButton.isClickable = !show
            doneButton.isClickable = !show
            cancelButton.isClickable = !show
        }
    }

    companion object {
        const val AUTH_ROUTE_EXTRA_KEY = "auth_route_extra_key"
    }
}
