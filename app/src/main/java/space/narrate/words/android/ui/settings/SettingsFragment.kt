package space.narrate.words.android.ui.settings

import android.content.ActivityNotFoundException
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.*
import space.narrate.words.android.billing.BillingConfig
import space.narrate.words.android.billing.BillingManager
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.configError
import space.narrate.words.android.ui.dialog.RadioGroupAlertDialog
import space.narrate.words.android.util.gone
import space.narrate.words.android.util.setUpWithElasticBehavior
import space.narrate.words.android.util.visible
import space.narrate.words.android.util.widget.BannerCardView
import space.narrate.words.android.util.widget.CheckPreferenceView
import space.narrate.words.android.util.widget.ElasticTransition
import javax.inject.Inject

/**
 * A [Fragment] that displays the main settings screen with an account banner (plugins and
 * important user prompts) and the most common settings like orientation lock, night mode,
 * sign out as well as subsequent settings views like about, contact and developer options
 *
 * [R.id.banner] Should show either a prompt to sign up/log in or publish the availability
 *  and status of the user's Merriam-Webster plugin
 * [R.id.night_mode_preference] Allows the user to switch between a light theme, a night theme or
 *  optionally allowing the user to have these set by time of day or the OS's settings
 * [R.id.orientation_preference] Allows the user to explicitly lock the app's orientation
 * [R.id.sign_out_preference] Should only show for registered users and allows the user to log out
 *  and sign in with different credentials or create a new account
 * [R.id.about_preference] Leads to [AboutFragment]
 * [R.id.contact_preference] Calls [Navigator.launchEmail]
 * [R.id.developer_preference] Leads to [DeveloperSettingsFragment] and is only shown for debug
 *  builds
 */
class SettingsFragment : BaseUserFragment(), BannerCardView.Listener {


    @Inject
    lateinit var billingManger: BillingManager

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var bannerCardView: BannerCardView
    private lateinit var nightModePreference: CheckPreferenceView
    private lateinit var orientationPreference: CheckPreferenceView
    private lateinit var signOutPreference: CheckPreferenceView
    private lateinit var aboutPreference: CheckPreferenceView
    private lateinit var contactPreference: CheckPreferenceView
    private lateinit var developerPreference: CheckPreferenceView

    private val sharedViewModel by lazy {
        ViewModelProviders
            .of(requireActivity(), viewModelFactory)
            .get(MainViewModel::class.java)
    }

    // This SettingsFragment's own ViewModel
    private val viewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)
            .get(SettingsViewModel::class.java)
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
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        appBarLayout = view.findViewById(R.id.app_bar)
        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        scrollView = view.findViewById(R.id.scroll_view)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        bannerCardView = view.findViewById(R.id.banner)
        nightModePreference = view.findViewById(R.id.night_mode_preference)
        orientationPreference = view.findViewById(R.id.orientation_preference)
        signOutPreference = view.findViewById(R.id.sign_out_preference)
        aboutPreference = view.findViewById(R.id.about_preference)
        contactPreference = view.findViewById(R.id.contact_preference)
        developerPreference = view.findViewById(R.id.developer_preference)


        appBarLayout.setUpWithElasticBehavior(
            this.javaClass.simpleName,
            sharedViewModel,
            listOf(navigationIcon),
            listOf(scrollView, appBarLayout)
        )

        navigationIcon.setOnClickListener {
            sharedViewModel.onNavigationIconClicked(this.javaClass.simpleName)
        }

        viewModel.shouldLaunchAuth.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { Navigator.launchAuth(requireContext(), it) }
        })

        viewModel.shouldLaunchMwPurchaseFlow.observe(this, Observer { event ->
            event.getUnhandledContent()?.let {
                billingManger.initiatePurchaseFlow(
                    requireActivity(),
                    BillingConfig.SKU_MERRIAM_WEBSTER
                )
            }
        })

        setUpBanner()

        setUpNightMode()

        setUpOrientation()

        // Sign out preference
        signOutPreference.setOnClickListener { viewModel.onSignOutClicked() }

        // About preference
        aboutPreference.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        }

        // Contact preference
        // If debug, there will be a developer settings item after this preference. Show divider
        contactPreference.setShowDivider(BuildConfig.DEBUG)
        contactPreference.setOnClickListener {
            try {
                Navigator.launchEmail(context!!, SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(
                    coordinatorLayout,
                    getString(R.string.settings_email_compose_no_client_error),
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

        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight
        )
        scrollView.updatePadding(
            bottom = insets.systemWindowInsetBottom
        )
        return super.handleApplyWindowInsets(insets)
    }

    private fun setUpBanner() {
        bannerCardView.setLisenter(this)
        viewModel.bannerModel.observe(this, Observer { model ->
            bannerCardView
                .setText(model.textRes)
                .setLabel(MwBannerModel.getConcatenatedLabel(
                    requireContext(),
                    model.labelRes,
                    model.daysRemaining
                ))
                .setTopButton(model.topButtonRes)
                .setBottomButton(model.bottomButtonRes)

            if (model.email == null) {
                signOutPreference.gone()
            } else {
                signOutPreference.setDesc(model.email)
                signOutPreference.visible()
            }
        })
    }

    private fun setUpNightMode() {
        nightModePreference.setOnClickListener { viewModel.onNightModePreferenceClicked() }

        viewModel.nightMode.observe(this, Observer { mode ->
            nightModePreference.setDesc(getString(mode.titleRes))
        })

        viewModel.shouldShowNightModeDialog.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { showNightModeDialog(it) }
        })
    }

    private fun setUpOrientation() {
        orientationPreference.setOnClickListener { viewModel.onOrientationPreferenceClicked() }

        viewModel.orientation.observe(this, Observer {
            orientationPreference.setDesc(getString(it.title))
        })

        viewModel.shouldShowOrientationDialog.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { showOrientationDialog(it) }
        })
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

    override fun onBannerClicked() {
        // Do nothing
    }

    override fun onBannerLabelClicked() {
        // Do nothing
    }

    override fun onBannerTopButtonClicked() {
        viewModel.onBannerTopButtonClicked()
    }

    override fun onBannerBottomButtonClicked() {
        viewModel.onBannerBottomButtonClicked()
    }

    companion object {
        const val SUPPORT_EMAIL_ADDRESS = "words@narrate.space"
    }
}
