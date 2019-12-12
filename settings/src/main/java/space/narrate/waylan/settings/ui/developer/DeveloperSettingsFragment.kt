package space.narrate.waylan.settings.ui.developer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.core.util.configError
import space.narrate.waylan.core.util.configInformative
import space.narrate.waylan.core.util.setUpWithElasticBehavior
import space.narrate.waylan.settings.R
import space.narrate.waylan.core.billing.BillingConfig
import space.narrate.waylan.settings.databinding.FragmentDeveloperSettingsBinding

/**
 * A Fragment to expose preferences to manipulate the configuration of Words, variables used
 * in conjunction with the current user (ie. directly changing Merriam-Webster PluginState
 * properties, clearing [UserPreferences]) and UI/UX elements (like Snackbar example triggers)
 *
 * [R.id.clear_user_preference] Resets all [UserPreferences] to their defaults.
 * [R.id.merriam_webster_state_preference] Changes the [PluginState] of the current user's
 *  Merriam-Webster plugin by manipulating properties on [User].
 * [R.id.use_test_skus_preference] Toggle to run Google Play Billing against test skus defined
 *  in [BillingConfig].
 * [R.id.merriam_webster_billing_response_preference] Toggle to switch between the three possible
 *  "fake" billing skus available for testing Google Play Billing.
 * [R.id.informative_snackbar_preference] Trigger an informative Snackbar to test UI/UX.
 * [R.id.error_snackbar_preference] Trigger an error Snackbar to test UI/UX.
 */
class DeveloperSettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentDeveloperSettingsBinding

    private val navigator: Navigator by inject()

    private val viewModel: DeveloperSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeveloperSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            appBar.setUpWithElasticBehavior(
                this.javaClass.simpleName,
                navigator,
                listOf(navigationIcon),
                listOf(scrollView, appBar)
            )

            navigationIcon.setOnClickListener {
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }

            viewModel.shouldShowSnackbar.observe(this@DeveloperSettingsFragment) { event ->
                event.getUnhandledContent()?.let { showSnackbar(it) }
            }

            clearUserPreference.setOnClickListener { viewModel.onClearPreferencesPreferenceClicked() }

            merriamWebsterStatePreference.setOnClickListener {
                viewModel.onMwStatePreferenceClicked()
            }

            viewModel.mwState.observe(this@DeveloperSettingsFragment) { state ->
                merriamWebsterStatePreference.setDesc(when (state) {
                    is PluginState.None -> "None"
                    is PluginState.FreeTrial ->
                        "Free trial (${if (state.isValid) "valid" else "expired"})"
                    is PluginState.Purchased ->
                        "Purchased (${if (state.isValid) "valid" else "expired"})"
                })
            }

            viewModel.useTestSkus.observe(this@DeveloperSettingsFragment) {
                useTestSkusPreference.setChecked(it)
            }
            useTestSkusPreference.setOnClickListener { viewModel.onUseTestSkusPreferenceClicked() }

            merriamWebsterBillingResponsePreference.setOnClickListener {
                viewModel.onMwBillingResponsePreferenceClicked()
            }
            viewModel.mwBillingResponse.observe(this@DeveloperSettingsFragment) {
                merriamWebsterBillingResponsePreference.setDesc(it)
            }

            informativeSnackbarPreference.setOnClickListener {
                viewModel.onInformativeSnackbarPreferenceClicked()
            }

            errorSnackbarPreference.setOnClickListener {
                viewModel.onErrorSnackbarPreferenceClicked()
            }
        }

        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        binding.run {
            coordinatorLayout.updatePadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight
            )
            scrollView.updatePadding(bottom = insets.systemWindowInsetBottom)
        }
        return super.handleApplyWindowInsets(insets)
    }

    private fun showSnackbar(model: SnackbarModel) {
        val length = when (model.length) {
            SnackbarModel.LENGTH_INDEFINITE -> Snackbar.LENGTH_INDEFINITE
            SnackbarModel.LENGTH_LONG -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        val snackbar = Snackbar.make(
            binding.coordinatorLayout,
            model.textRes,
            length
        )
        if (model.isError) {
            snackbar.configError(requireContext())
        } else {
            snackbar.configInformative(requireContext())
        }
        snackbar.show()
    }
}
