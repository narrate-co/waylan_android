package space.narrate.words.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.NavDestination
import androidx.transition.Transition
import space.narrate.words.android.ui.auth.AuthActivity
import space.narrate.words.android.ui.auth.AuthRoute
import space.narrate.words.android.ui.details.DetailsFragment
import space.narrate.words.android.ui.home.HomeFragment
import space.narrate.words.android.ui.list.ListFragment
import space.narrate.words.android.ui.list.ListFragmentArgs
import space.narrate.words.android.ui.list.ListType
import space.narrate.words.android.ui.settings.*
import space.narrate.words.android.util.widget.ElasticTransition

object Navigator {

    enum class Destination {
        HOME,
        TRENDING,
        RECENT,
        FAVORITE,
        DETAILS,
        SETTINGS,
        ABOUT,
        THIRD_PARTY,
        DEV_SETTINGS;

        companion object {
            fun fromDestinationId(destination: NavDestination, args: Bundle?): Destination {
                return when (destination.id) {
                    R.id.homeFragment -> HOME
                    R.id.listFragment -> when (ListFragmentArgs.fromBundle(args!!).listType) {
                        ListType.TRENDING -> TRENDING
                        ListType.RECENT -> RECENT
                        ListType.FAVORITE -> FAVORITE
                    }
                    R.id.detailsFragment -> DETAILS
                    R.id.settingsFragment -> SETTINGS
                    R.id.aboutFragment -> ABOUT
                    R.id.thirdPartyLibrariesFragment -> THIRD_PARTY
                    R.id.developerSettingsFragment -> DEV_SETTINGS
                    else -> HOME
                }
            }
        }
    }

    /**
     * Launch [AuthActivity]. The passed [AuthActivity.AuthRoute] will be used by the launched
     * [AuthActivity] to determine what behavior should be expected/UI configuration should be
     * given.
     *
     * Note: [AuthActivity] and [RouterActivity] are the only two Activities which do not require
     * a [UserScope]/valid user.
     *
     * @param authRoute The listType of action launching this [AuthActivity] should accomplish
     * @param filterIntent Any intent extras which should be passed through this [AuthActivity]
     *  and on to any subsequent destinations who wish to consume them. (ie. an
     *  Intent.ACTION_PROCESS_TEXT extra)
     */
    fun launchAuth(
        context: Context,
        authRoute: AuthRoute? = null,
        filterIntent: Intent? = null
    ) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (authRoute != null) intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }

    /**
     * Launch [MainActivity].
     *
     * @param clearStack true if pressing back from this new [MainActivity] should exit the app
     * @param filterIntent Any intent extras which should be passed to this [MainActivity]. These
     *  may be intents which were received by [RouterActivity] or [AuthActivity] and should now
     *  be handled by [MainActivity], such as Intent.ACTION_PROCCESS_TEXT extras.
     */
    fun launchMain(context: Context, clearStack: Boolean, filterIntent: Intent?) {
        val intent = Intent(context, MainActivity::class.java)
        if (clearStack) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }

    /**
     * Launch the user's default email client directly into a newly composed email.
     *
     * @param toEmail The addressee's email address
     * @param subject The email titleRes
     * @param body The email's content
     * @param shareTitle The titleRes of the share picker sheet the client will be offered to choose
     *  their desired email client
     * @throws ActivityNotFoundException If the user doesn't have an email client installed, this
     *  method will throw an ActivityNotFound exception
     */
    @Throws(ActivityNotFoundException::class)
    fun launchEmail(context: Context, toEmail: String, subject: String = "", body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO)
        val mailTo = "mailto:$toEmail?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
        intent.data = Uri.parse(mailTo)
        context.startActivity(intent)
    }

    /**
     * Open a [url] with the default browser
     */
    fun launchWebsite(context: Context, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}

