package space.narrate.waylan.settings.ui.settings

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.core.util.configError
import space.narrate.waylan.core.util.launchEmail
import space.narrate.waylan.settings.BuildConfig
import space.narrate.waylan.settings.R
import space.narrate.waylan.settings.databinding.FragmentSettingsBinding
import space.narrate.waylan.settings.ui.dialog.RadioGroupAlertDialog

/**
 * A [Fragment] that displays the main settings screen with an account banner (plugins and
 * important user prompts) and the most common settings like orientation lock, night mode,
 * sign out as well as subsequent settings views like about, contact and developer options
 *
 * [R.id.night_mode_preference] Allows the user to switch between a light theme, a night theme or
 *  optionally allowing the user to have these set by time of day or the OS's settings
 * [R.id.orientation_preference] Allows the user to explicitly lock the app's orientation
 * [R.id.log_in_sign_out_preference] Should only show for registered users and allows the user to log out
 *  and sign in with different credentials or create a new account
 * [R.id.about_preference] Leads to [AboutFragment]
 * [R.id.contact_preference] Calls [ContextExtensions.launchEmail]
 * [R.id.developer_preference] Leads to [DeveloperSettingsFragment] and is only shown for debug
 *  builds
 */
class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val navigator: Navigator by inject()

    private val viewModel: SettingsViewModel by viewModel()

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
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.shouldLaunchLogIn.observe(this) { event ->
            event.withUnhandledContent { navigator.toLogIn(requireContext()) }
        }

        viewModel.shouldLaunchSignUp.observe(this) { event ->
            event.withUnhandledContent { navigator.toSignUp(requireContext()) }
        }

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

            appBar.setReachableContinuityNavigator(this@SettingsFragment, navigator)

            addOnsPreference.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_addOnsFragment)
            }

            setUpNightMode()

            setUpOrientation()

            // Sign out preference
            logInSignOutPreference.setOnClickListener { viewModel.onSignOutClicked() }
            viewModel.logInSignOut.observe(this@SettingsFragment) { model ->
                logInSignOutPreference.setTitle(getString(model.titleRes))
                logInSignOutPreference.setDesc(model.getDesc(requireContext()))
            }

            // About preference
            aboutPreference.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
            }

            // Contact preference
            // If debug, there will be a developer settings item after this preference. Show divider
            contactPreference.setShowDivider(BuildConfig.DEBUG)
            contactPreference.setOnClickListener {
                try {
                    requireContext().launchEmail(SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(
                        coordinatorLayout,
                        R.string.settings_email_compose_no_client_error,
                        Snackbar.LENGTH_SHORT
                    )
                        .configError(requireContext())
                        .show()
                }
            }

            // Developer settings preference, only shown if this is a debug build
            // TODO further lock this down. Possibly by user?
            developerPreference.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
            developerPreference.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_developerSettingsFragment)
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
            scrollView.updatePadding(
                bottom = insets.systemWindowInsetBottom
            )
        }
        return super.handleApplyWindowInsets(insets)
    }

    private fun setUpNightMode() {
        binding.nightModePreference.setOnClickListener { viewModel.onNightModePreferenceClicked() }

        viewModel.nightMode.observe(this) { mode ->
            binding.nightModePreference.setDesc(getString(mode.titleRes))
        }

        viewModel.shouldShowNightModeDialog.observe(this) { event ->
            event.withUnhandledContent { showNightModeDialog(it) }
        }
    }

    private fun setUpOrientation() {
        binding.orientationPreference.setOnClickListener { viewModel.onOrientationPreferenceClicked() }

        viewModel.orientation.observe(this) {
            binding.orientationPreference.setDesc(getString(it.title))
        }

        viewModel.shouldShowOrientationDialog.observe(this) { event ->
            event.withUnhandledContent { showOrientationDialog(it) }
        }
    }

    private fun showNightModeDialog(items: List<NightModeRadioItemModel>) {
        RadioGroupAlertDialog(requireContext(), items)
            .onItemSelected { item ->
                viewModel.onNightModeSelected(item)
                true
            }
            .show()
    }

    private fun showOrientationDialog(items: List<OrientationRadioItemModel>) {
        RadioGroupAlertDialog(requireContext(), items)
            .onItemSelected { item ->
                viewModel.onOrientationSelected(item)
                true
            }
            .show()
    }

    companion object {
        const val SUPPORT_EMAIL_ADDRESS = "waylan@narrate.space"
    }
}
