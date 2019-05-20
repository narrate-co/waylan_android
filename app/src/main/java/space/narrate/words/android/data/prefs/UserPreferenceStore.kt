package space.narrate.words.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import space.narrate.words.android.ui.search.Period

/**
 *
 * A top-level store for user-tied [SharedPreferences]. This makes it simpler to support multiple
 * users on a single device.
 *
 * This class should not be used directly by ViewModels. Instead, user [UserRepository], which
 * further abstracts Preferences. Where these specific settings could change in the future (ie.
 * be moved into Firestore). If that happens, using [UserRepository] will ensure no UI/ViewModel
 * logic needs to change.
 */
class UserPreferenceStore(
        private val applicationContext: Context,
        userId: String? = null
) {


    private val sharedPrefs: SharedPreferences by lazy {
        if (userId == null) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        } else {
            applicationContext.getSharedPreferences(userId, Context.MODE_PRIVATE)
        }
    }

    /**
     * Reset all preferences to their default state
     */
    fun resetAll() {
        hasSeenRecentsBanner = false
        hasSeenFavoritesBanner = false
        hasSeenTrendingBanner = false
        hasSeenDragDismissOverlay = false
        hasSeenMerriamWebsterPermissionPane = false
        setRecentsListFilter(emptyList())
        setTrendingListFilter(emptyList())
        setFavoritesListFilter(emptyList())
        useTestSkus = false
        portraitToLandscapeOrientationChangeCount = 0L
        landscapeToPortraitOrientationChangeCount = 0L
    }

    /** User scoped preferences */

    // Recents banner
    var hasSeenRecentsBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_RECENTS_BANNER,
            false
    )
    val hasSeenRecentsBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_RECENTS_BANNER, false)



    // Favorites banner
    var hasSeenFavoritesBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_FAVORITES_BANNER,
            false
    )
    val hasSeenFavoritesBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_FAVORITES_BANNER, false)



    // Trending banner
    var hasSeenTrendingBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_TRENDING_BANNER,
            false
    )
    val hasSeenTrendingBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_TRENDING_BANNER, false)


    // Drag dismiss overlay
    var hasSeenDragDismissOverlay: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_DRAG_DISMISS_OVERLAY,
            false
    )


    // Merriam-Webster Permission Pane
    var hasSeenMerriamWebsterPermissionPane: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE,
            false
    )
    val hasSeenMerriamWebsterPermissionPaneLive: LiveData<Boolean> =
        PreferenceLiveData(
            sharedPrefs,
            UserPreferences.HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE,
            false
        )

    // Recents list filter
    private var recentsListFilter: Set<String> by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.RECENTS_LIST_FILTER,
            emptySet()
    )
    fun setRecentsListFilter(list: List<Period>) {
        recentsListFilter = list.map { it.prefString }.toSet()
    }

    fun getRecentsListFilter(): List<Period> {
        return recentsListFilter.toList().map { Period.fromPrefString(it) }
    }

    val recentsListFilterLive: LiveData<List<Period>> =
            Transformations.map(PreferenceLiveData<Set<String>>(
                    sharedPrefs,
                    UserPreferences.RECENTS_LIST_FILTER,
                    emptySet())
            ) { set ->
                set.toList().map { Period.fromPrefString(it) }
            }



    // Trending list filter
    private var trendingListFilter: Set<String> by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.TRENDING_LIST_FILTER,
            emptySet()
    )

    fun getTrendingListFilter(): List<Period> {
        return trendingListFilter.toList().map { Period.fromPrefString(it) }
    }

    fun setTrendingListFilter(list: List<Period>) {
        trendingListFilter = list.map { it.prefString }.toSet()
    }

    val trendingListFilterLive: LiveData<List<Period>> =
            Transformations.map(PreferenceLiveData<Set<String>>(
                    sharedPrefs,
                    UserPreferences.TRENDING_LIST_FILTER,
                    emptySet())
            ) { set ->
                set.toList().map { Period.fromPrefString(it) }
            }



    // Favorites list filter
    private var favoritesListFilter: Set<String> by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.FAVORITES_LIST_FILTER,
            emptySet()
    )
    fun setFavoritesListFilter(list: List<Period>) {
        favoritesListFilter = list.map { it.prefString }.toSet()
    }
    fun getFavoritesListFilter(): List<Period> {
        return favoritesListFilter.toList().map { Period.fromPrefString(it) }
    }
    val favoritesListFilterLive: LiveData<List<Period>> =
            Transformations.map(PreferenceLiveData<Set<String>>(
                    sharedPrefs,
                    UserPreferences.FAVORITES_LIST_FILTER,
                    emptySet())
            ) { set ->
                set.toList().map { Period.fromPrefString(it) }
            }



    // Billing test sku
    var useTestSkus: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.USE_TEST_SKUS,
            false
    )
    val useTestSkusLive: LiveData<Boolean> =
            PreferenceLiveData(sharedPrefs, UserPreferences.USE_TEST_SKUS, false)




    // portrait to landscape orientation change
    var portraitToLandscapeOrientationChangeCount: Long by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT,
            0L
    )

    // landscape to portrait orientation change
    var landscapeToPortraitOrientationChangeCount: Long by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT,
            0L
    )

}

