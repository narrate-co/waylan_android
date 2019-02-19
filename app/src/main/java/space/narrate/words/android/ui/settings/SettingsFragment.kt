package space.narrate.words.android.ui.settings

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.*
import space.narrate.words.android.billing.BillingConfig
import space.narrate.words.android.billing.BillingManager
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.ui.auth.AuthActivity
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.configError
import kotlinx.android.synthetic.main.fragment_settings.view.*
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.ui.dialog.NightModeDialog
import space.narrate.words.android.ui.dialog.OrientationDialog
import space.narrate.words.android.util.gone
import space.narrate.words.android.util.visible
import javax.inject.Inject

/**
 * A [Fragment] that displays the main settings screen with an account banner (plugins and
 * important user prompts) and the most common settings like orientation lock, night mode,
 * sign out as well as subsequent settings views like about, contact and developer options
 *
 * [R.id.banner] Should show either a prompt to sign up/log in or publish the availability
 *  and status of the user's Merriam-Webster plugin
 * [R.id.nightMode] Allows the user to switch between a light theme, a night theme or optionally
 * allowing the user to have these set by time of day or the OS's settings
 * [R.id.orientation] Allows the user to explicitly lock the app's orientation
 * [R.id.signOut] Should only show for registered users and allows the user to log out and sign in
 *  with different credentials or create a new account
 * [R.id.about] Leads to [AboutFragment]
 * [R.id.contact] Calls [Navigator.launchEmail]
 * [R.id.developer] Leads to [DeveloperSettingsFragment] and is only shown for debug builds
 */
class SettingsFragment : BaseUserFragment() {

    @Inject
    lateinit var billingManger: BillingManager

    companion object {
        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "settings_fragment_tag"

        fun newInstance() = SettingsFragment()

        const val SUPPORT_EMAIL_ADDRESS = "words@narrate.space"
    }

    // This SettingsFragment's own ViewModel
    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }
        // Observe the user and use value to configure this Fragment's views. This makes it easy
        // to set [User] properties and have those changes automatically be conusmed by this
        // observable and update the views as necessary
        viewModel.userLive.observe(this, Observer {
            setSettings(view, it)
        })
        return view
    }

    /**
     * Top-level helper function to parse [user] and set preferences appropriately
     */
    private fun setSettings(view: View, user: User?) {
        // Set state specific settings
        when {
            user == null || user.isAnonymous -> setAsAnonymous(view, user)
            else -> setAsRegistered(view, user)
        }

        // Set settings common to all states

        // Night mode preference
        viewModel.nightModeLive.observe(this, Observer {
            val desc = when (it) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> getString(R.string.settings_night_mode_follows_system_title)
                AppCompatDelegate.MODE_NIGHT_AUTO -> getString(R.string.settings_night_mode_auto_title)
                AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.settings_night_mode_yes_title)
                AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.settings_night_mode_no_title)
                else -> getString(R.string.settings_night_mode_follows_system_title)
            }
            view.nightMode.setDesc(desc)
        })

        val nightModeCallback = object: NightModeDialog.NightModeCallback() {
            var selected: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            override fun onSelected(nightMode: Int) {
                selected = nightMode
            }

            override fun onDismissed() {
                viewModel.nightMode = selected
                (activity?.application as? App)?.updateNightMode()
            }
        }

        view.nightMode.setOnClickListener {
            // TODO move into Naviagator
            NightModeDialog
                    .newInstance(viewModel.nightMode, nightModeCallback)
                    .show(activity?.supportFragmentManager, "night_mode")
        }

        // Orientation lock preference
        viewModel.orientationLive.observe(this, Observer {
            view.orientation.setDesc(getString(it.title))
        })

        val orientationCallback = object: OrientationDialog.OrientationCallback() {
            var selected = Orientation.UNSPECIFIED
            override fun onSelected(orientation: Orientation) {
                selected = orientation
            }
            override fun onDismissed() {
                viewModel.orientation = selected
                (activity?.application as? App)?.updateOrientation()
            }
        }

        view.orientation.setOnClickListener {
            // TODO move into Navigator
            OrientationDialog
                    .newInstance(viewModel.orientation, orientationCallback)
                    .show(activity?.supportFragmentManager, "orientation")
        }

        // About preference
        view.about.setOnClickListener {
            (activity as? SettingsActivity)?.showAbout()
        }

        // Contact preference
        // If debug, there will be a developer settings item after this preference. Show divider
        view.contact.setShowDivider(BuildConfig.DEBUG)
        view.contact.setOnClickListener {
            try {
                Navigator.launchEmail(context!!, SUPPORT_EMAIL_ADDRESS, getString(R.string.settings_email_compose_subject))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(
                        view.settingsRoot,
                        getString(R.string.settings_email_compose_no_client_error),
                        Snackbar.LENGTH_SHORT
                )
                        .configError(context!!, false)
                        .show()
            }
        }

        // Developer settings preference
        // Only show if this is a debug build
        // TODO further lock this down. Possibly by [User]?
        view.developer.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        view.developer.setOnClickListener {
            (activity as? SettingsActivity)?.showDeveloperSettings()
        }
    }

    /**
     * Set preferences specific to a user who is anonymous.
     *
     * For example, this includes a banner to urge sign up/log in instead of a banner to promote
     * plugins
     */
    private fun setAsAnonymous(view: View, user: User?) {
        val state = user?.merriamWebsterState ?: PluginState.None()

        // Banner
        when (state) {
            is PluginState.None -> {
                view.banner.setBannerText(
                        getString(R.string.settings_header_anonymous_none_body)
                )
                view.banner.setBannerLabelText(null)
            }
            is PluginState.FreeTrial -> {
                val label = if (state.isValid) {
                    "Free Trial: ${state.remainingDays}d"
                } else {
                    "Free trial expired"
                }
                view.banner.setBannerLabelText(label)
                view.banner.setBannerText(
                        getString(R.string.settings_header_anonymous_free_trial_body)
                )
            }
            is PluginState.Purchased -> {
                //This should never happen
                view.banner.setBannerText(
                        getString(R.string.settings_header_anonymous_none_body)
                )
                view.banner.setBannerLabelText(null)
            }
        }


        view.banner.setBannerTopButton(getString(R.string.settings_header_anonymous_create_account_button), View.OnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.SIGN_UP)
        })

        view.banner.setBannerBottomButton(getString(R.string.settings_header_anonymous_log_in_button), View.OnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        })

        // Sign out preference
        view.signOut.gone()

        view.banner.visible()
    }

    /**
     * Set preferences specific to the [user] who is registered
     *
     * For example, this will show a banner promoting plugins since there is no need to urge
     * the user to register any longer.
     */
    private fun setAsRegistered(view: View, user: User) {
        val state = user.merriamWebsterState

        // Banner
        when (state) {
            is PluginState.None -> {
                view.banner.setBanner(
                        text = getString(R.string.settings_header_registered_none_body),
                        topButton = getString(R.string.settings_header_registered_add_button)
                )
            }
            is PluginState.FreeTrial -> {
                if (state.isValid) {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_free_trial_body),
                            label = "Free trial: ${state.remainingDays}d",
                            topButton = getString(R.string.settings_header_registered_add_button),
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                } else {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_free_trial_expired_body),
                            label = "Free trial expired",
                            topButton = getString(R.string.settings_header_registered_add_button),
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                }
            }
            is PluginState.Purchased -> {
                if (state.isValid) {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_subscribed_body),
                            label = "Added"
                    )
                } else {
                    view.banner.setBanner(
                            text = getString(R.string.settings_header_registered_subscribed_expired_body),
                            label = "Plugin expired",
                            topButton = "Renew",
                            topButtonListener = View.OnClickListener {
                                launchMerriamWebsterPurchaseFlow()
                            }
                    )
                }

            }
        }

        // Sign out preference
        if (user.email.isNotBlank()) {
            view.signOut.setDesc(user.email)
        }
        view.signOut.setOnClickListener {
            Navigator.launchAuth(context!!, AuthActivity.AuthRoute.LOG_IN)
        }
    }

    /**
     * Call the Google Play Billing library to launch a new purchase flow
     */
    private fun launchMerriamWebsterPurchaseFlow() {
        billingManger.initiatePurchaseFlow(activity!!, BillingConfig.SKU_MERRIAM_WEBSTER)
    }

}
