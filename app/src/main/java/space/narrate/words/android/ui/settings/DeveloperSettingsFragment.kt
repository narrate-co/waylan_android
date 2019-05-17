package space.narrate.words.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.R
import space.narrate.words.android.billing.BillingConfig
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.ui.common.SnackbarModel
import space.narrate.words.android.util.configError
import space.narrate.words.android.util.configInformative
import space.narrate.words.android.util.widget.CheckPreferenceView
import space.narrate.words.android.util.widget.ElasticTransition


/**
 * A Fragment to expose preferences to manipulate the configuration of Words, variables used
 * in conjunction with the current user (ie. directly changing Merriam-Webster PluginState
 * properties, clearing [UserPreferences]) and UI/UX elements (like Snackbar example triggers)
 *
 * [R.id.clear_user_preference] Resets all [UserPreferences] to their defaults
 * [R.id.merriam_webster_state_preference] Changes the [PluginState] of the current user's Merriam-Webster plugin
 *  by manipulating properties on [User]
 * [R.id.use_test_skus_preference] Toggle to run Google Play Billing against test skus defined in [BillingConfig]
 * [R.id.merriam_webster_billing_response_preference] Toggle to switch between the three possible "fake" billing
 *  skus available for testing Google Play Billing
 * [R.id.informative_snackbar_preference] Trigger an informative Snackbar to test UI/UX
 * [R.id.error_snackbar_preference] Trigger an error Snackbar to test UI/UX
 */
class DeveloperSettingsFragment : BaseUserFragment() {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var clearUserPreference: CheckPreferenceView
    private lateinit var mwStatePreference: CheckPreferenceView
    private lateinit var useTestSkusPreference: CheckPreferenceView
    private lateinit var mwBillingResponsePreference: CheckPreferenceView
    private lateinit var informativeSnackbarPreference: CheckPreferenceView
    private lateinit var errorSnackbarPreference: CheckPreferenceView

    // Reuse the SettingsViewView model from SettingsFragment since many of the methods are the same
    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(DeveloperSettingsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_developer_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        scrollView = view.findViewById(R.id.scroll_view)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        clearUserPreference = view.findViewById(R.id.clear_user_preference)
        mwStatePreference = view.findViewById(R.id.merriam_webster_state_preference)
        useTestSkusPreference = view.findViewById(R.id.use_test_skus_preference)
        mwBillingResponsePreference = view.findViewById(
            R.id.merriam_webster_billing_response_preference
        )
        informativeSnackbarPreference = view.findViewById(R.id.informative_snackbar_preference)
        errorSnackbarPreference = view.findViewById(R.id.error_snackbar_preference)

        navigationIcon.setOnClickListener { requireActivity().onBackPressed() }

        viewModel.shouldShowSnackbar.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { showSnackbar(it) }
        })

        clearUserPreference.setOnClickListener { viewModel.onClearPreferencesPreferenceClicked() }

        mwStatePreference.setOnClickListener {
            viewModel.onMwStatePreferenceClicked()
        }

        viewModel.mwState.observe(this, Observer { state ->
            mwStatePreference.setDesc(when (state) {
                is PluginState.None -> "None"
                is PluginState.FreeTrial ->
                    "Free trial (${if (state.isValid) "valid" else "expired"})"
                is PluginState.Purchased ->
                    "Purchased (${if (state.isValid) "valid" else "expired"})"
            })
        })

        viewModel.useTestSkus.observe(this, Observer {
            useTestSkusPreference.setChecked(it)
        })
        useTestSkusPreference.setOnClickListener { viewModel.onUseTestSkusPreferenceClicked() }

        mwBillingResponsePreference.setOnClickListener {
            viewModel.onMwBillingResponsePreferenceClicked()
        }
        viewModel.mwBillingResponse.observe(this, Observer {
            mwBillingResponsePreference.setDesc(it)
        })

        informativeSnackbarPreference.setOnClickListener {
            viewModel.onInformativeSnackbarPreferenceClicked()
        }

        errorSnackbarPreference.setOnClickListener {
            viewModel.onErrorSnackbarPreferenceClicked()
        }
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight
        )
        scrollView.updatePadding(bottom = insets.systemWindowInsetBottom)
        return super.handleApplyWindowInsets(insets)
    }

    private fun showSnackbar(model: SnackbarModel) {
        val snackbar = Snackbar.make(coordinatorLayout, model.textRes, when (model.length) {
            SnackbarModel.LENGTH_INDEFINITE -> Snackbar.LENGTH_INDEFINITE
            SnackbarModel.LENGTH_LONG -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        })
        if (model.isError) {
            snackbar.configError(requireContext())
        } else {
            snackbar.configInformative(requireContext())
        }
        snackbar.show()
    }
}
