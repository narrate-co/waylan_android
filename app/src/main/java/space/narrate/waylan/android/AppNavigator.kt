package space.narrate.waylan.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDestination
import space.narrate.waylan.android.ui.MainActivity
import space.narrate.waylan.android.ui.auth.AuthActivity
import space.narrate.waylan.android.ui.auth.AuthRoute
import space.narrate.waylan.android.ui.list.ListFragmentArgs
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.ListType
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.Navigator.BackType.DRAG
import space.narrate.waylan.core.ui.Navigator.BackType.ICON
import space.narrate.waylan.core.ui.common.Event

class AppNavigator(
    private val analyticsRepository: AnalyticsRepository
) : Navigator {

    private val _currentDestination: MutableLiveData<Destination> = MutableLiveData()
    override val currentDestination: LiveData<Destination>
        get() = _currentDestination

    private val _shouldNavigateBack: MutableLiveData<Event<Boolean>> = MutableLiveData()
    override val shouldNavigateBack: LiveData<Event<Boolean>>
        get() = _shouldNavigateBack

    override fun setCurrentDestination(dest: NavDestination, arguments: Bundle?) {
        _currentDestination.value = fromDestinationId(dest, arguments)
    }

    override fun toBack(backType: Navigator.BackType, from: String): Boolean {
        when (backType) {
            ICON -> analyticsRepository.logNavigationIconEvent(from)
            DRAG -> analyticsRepository.logDragDismissEvent(from)
        }
        _shouldNavigateBack.value = Event(true)
        return true
    }

    /**
     * Launch [MainActivity].
     *
     * @param clearStack true if pressing back from this new [MainActivity] should exit the app
     * @param filterIntent Any intent extras which should be passed to this [MainActivity]. These
     *  may be intents which were received by [RouterActivity] or [AuthActivity] and should now
     *  be handled by [MainActivity], such as Intent.ACTION_PROCCESS_TEXT extras.
     */
    override fun toHome(context: Context, clearStack: Boolean, filterIntent: Intent?) {
        val intent = Intent(context, MainActivity::class.java)
        if (clearStack) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }

    override fun toAuth(context: Context, filterIntent: Intent?) {
        launchAuth(context, filterIntent = filterIntent)
    }

    override fun toLogIn(context: Context, filterIntent: Intent?) {
        launchAuth(context, AuthRoute.LOG_IN, filterIntent)
    }

    override fun toSignUp(context: Context, filterIntent: Intent?) {
        launchAuth(context, AuthRoute.SIGN_UP, filterIntent)
    }

    private fun launchAuth(
        context: Context,
        authRoute: AuthRoute? = null,
        filterIntent: Intent? = null
    )  {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (authRoute != null) intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }


    companion object {
        fun fromDestinationId(destination: NavDestination, args: Bundle?): Destination {
            return when (destination.id) {
                R.id.homeFragment -> Destination.HOME
                R.id.listFragment -> when (ListFragmentArgs.fromBundle(args!!).listType) {
                    ListType.TRENDING -> Destination.TRENDING
                    ListType.RECENT -> Destination.RECENT
                    ListType.FAVORITE -> Destination.FAVORITE
                    else -> Destination.RECENT
                }
                R.id.detailsFragment -> Destination.DETAILS
                R.id.settingsFragment -> Destination.SETTINGS
                R.id.aboutFragment -> Destination.ABOUT
                R.id.thirdPartyLibrariesFragment -> Destination.THIRD_PARTY
                R.id.developerSettingsFragment -> Destination.DEV_SETTINGS
                else -> Destination.HOME
            }
        }
    }
}

