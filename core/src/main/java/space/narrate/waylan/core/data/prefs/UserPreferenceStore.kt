package space.narrate.waylan.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.util.DefaultingMutableLiveData

/**
 *
 * A top-level store for user-tied [SharedPreferences]. This makes it simpler to support multiple
 * users on a single device.
 *
 * This class should not be used directly by ViewModels. Instead, user [UserRepository], which
 * further abstracts Preferences. Where these specific settings could change in the future (ie.
 * be moved into Firestore). If that happens, using [UserRepository] will ensure no UI/ViewModel
 * logic needs to change.
 *
 */
class UserPreferenceStore(
    private val applicationContext: Context,
    authenticationStore: AuthenticationStore
) {

    private var sharedPrefs = DefaultingMutableLiveData(
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    )

    init {
        authenticationStore.uid.observeForever {
            sharedPrefs.value = applicationContext.getSharedPreferences(
                it,
                Context.MODE_PRIVATE
            )
        }
    }

    /** User scoped preferences */

    val hasSeenRecentsBanner = Preference(
        UserPreferences.HAS_SEEN_RECENTS_BANNER,
        false,
        sharedPrefs
    )

    val hasSeenFavoritesBanner = Preference(
        UserPreferences.HAS_SEEN_FAVORITES_BANNER,
        false,
        sharedPrefs
    )

    val hasSeenTrendingBanner = Preference(
        UserPreferences.HAS_SEEN_TRENDING_BANNER,
        false,
        sharedPrefs
    )

    val hasSeenDragDismissOverlay = Preference(
        UserPreferences.HAS_SEEN_DRAG_DISMISS_OVERLAY,
        false,
        sharedPrefs
    )

    val recentsListFilter = BoxedPreference(
        UserPreferences.RECENTS_LIST_FILTER,
        emptyList<Period>(),
        sharedPrefs,
        { it.toStringSet },
        { it.toPeriodList }
    )

    val trendingListFilter = BoxedPreference(
        UserPreferences.TRENDING_LIST_FILTER,
        emptyList<Period>(),
        sharedPrefs,
        { it.toStringSet },
        { it.toPeriodList }
    )

    val favoritesListFilter = BoxedPreference(
        UserPreferences.FAVORITES_LIST_FILTER,
        emptyList<Period>(),
        sharedPrefs,
        { it.toStringSet },
        { it.toPeriodList }
    )

    val useTestSkus = Preference(
        UserPreferences.USE_TEST_SKUS,
        false,
        sharedPrefs
    )

    val portraitToLandscapeOrientationChangeCount = Preference(
        UserPreferences.PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT,
        0L,
        sharedPrefs
    )

    val landscapeToPortraitOrientationChangeCount = Preference(
        UserPreferences.LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT,
        0L,
        sharedPrefs
    )

    private val List<Period>.toStringSet: Set<String>
        get() = map { it.prefString }.toSet()

    private val Set<String>.toPeriodList: List<Period>
        get() = toList().map { Period.fromPrefString(it) }

    /**
     * Reset all preferences to their default state
     */
    fun resetAll() {
        hasSeenRecentsBanner.clear()
        hasSeenFavoritesBanner.clear()
        hasSeenTrendingBanner.clear()
        hasSeenDragDismissOverlay.clear()
//        hasSeenMerriamWebsterPermissionPane.clear()
        recentsListFilter.clear()
        trendingListFilter.clear()
        favoritesListFilter.clear()
        useTestSkus.clear()
        portraitToLandscapeOrientationChangeCount.clear()
        landscapeToPortraitOrientationChangeCount.clear()
    }
}

