package space.narrate.waylan.settings.ui.developer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.core.billing.BillingConfig
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.make
import space.narrate.waylan.settings.R
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
class DeveloperSettingsFragment : Fragment() {

    private lateinit var binding: FragmentDeveloperSettingsBinding

    private val navigator: Navigator by inject()

    private val viewModel: DeveloperSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeveloperSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {

            appBar.doOnElasticDrag(
                alphaViews = listOf(scrollView, appBar)
            )

            appBar.doOnElasticDismiss {
                navigator.toBack(Navigator.BackType.DRAG, this.javaClass.simpleName)
            }

            appBar.setOnNavigationIconClicked {
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }

            appBar.setReachableContinuityNavigator(this@DeveloperSettingsFragment, navigator)

            viewModel.shouldShowSnackbar.observe(viewLifecycleOwner) { event ->
                event.withUnhandledContent { it.make(binding.coordinatorLayout).show() }
            }

            clearUserPreference.setOnClickListener { viewModel.onClearPreferencesPreferenceClicked() }

            viewModel.isAnonymousUser.observe(viewLifecycleOwner) {
                isAnonymousUserPreference.setChecked(it)
            }
            isAnonymousUserPreference.setOnClickListener { viewModel.onIsAnonymousUserPreferenceClicked() }

            merriamWebsterStatePreference.setOnClickListener {
                viewModel.onMwStatePreferenceClicked()
            }

            merriamWebsterStateThesaurusPreference.setOnClickListener {
                viewModel.onMwThesaurusPreferenceClicked()
            }

            americanHeritageStatePreference.setOnClickListener {
                viewModel.onAdhPreferenceClicked()
            }

            viewModel.mwState.observe(viewLifecycleOwner) { state ->
                merriamWebsterStatePreference.setDesc(state)
            }

            viewModel.mwThesaurusState.observe(viewLifecycleOwner) { state ->
                merriamWebsterStateThesaurusPreference.setDesc(state)
            }

            viewModel.ahdState.observe(viewLifecycleOwner) { state ->
                americanHeritageStatePreference.setDesc(state)
            }

            viewModel.useTestSkus.observe(viewLifecycleOwner) {
                useTestSkusPreference.setChecked(it)
            }
            useTestSkusPreference.setOnClickListener { viewModel.onUseTestSkusPreferenceClicked() }

            billingResponsePreference.setOnClickListener {
                viewModel.onBillingResponsePreferenceClicked()
            }
            viewModel.billingResponse.observe(viewLifecycleOwner) {
                billingResponsePreference.setDesc(it)
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
}
