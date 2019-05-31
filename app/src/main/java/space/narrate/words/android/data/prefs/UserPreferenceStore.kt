package space.narrate.words.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.DefaultingMutableLiveData
import space.narrate.words.android.util.mapTransform
import space.narrate.words.android.util.switchMapTransform

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
        authenticationStore.user.observeForever {
            sharedPrefs.value = applicationContext.getSharedPreferences(
                it.uid,
                Context.MODE_PRIVATE
            )
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
        recentsListFilter = emptyList()
        trendingListFilter = emptyList()
        favoritesListFilter = emptyList()
        useTestSkus = false
        portraitToLandscapeOrientationChangeCount = 0L
        landscapeToPortraitOrientationChangeCount = 0L
    }

    /** User scoped preferences */

    var hasSeenRecentsBanner: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(
            UserPreferences.HAS_SEEN_RECENTS_BANNER,
            false
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.HAS_SEEN_RECENTS_BANNER, value)
        }

    val hasSeenRecentsBannerLive: LiveData<Boolean> = sharedPrefs.switchMapTransform {
        PreferenceLiveData(it, UserPreferences.HAS_SEEN_RECENTS_BANNER, false)
    }

    var hasSeenFavoritesBanner: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(
            UserPreferences.HAS_SEEN_FAVORITES_BANNER,
            false
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.HAS_SEEN_FAVORITES_BANNER, value)
        }

    val hasSeenFavoritesBannerLive: LiveData<Boolean> = sharedPrefs.switchMapTransform {
        PreferenceLiveData(it, UserPreferences.HAS_SEEN_FAVORITES_BANNER, false)
    }

     var hasSeenTrendingBanner: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(
            UserPreferences.HAS_SEEN_TRENDING_BANNER,
            false
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.HAS_SEEN_TRENDING_BANNER, value)
        }

    val hasSeenTrendingBannerLive: LiveData<Boolean> = sharedPrefs.switchMapTransform {
        PreferenceLiveData(it, UserPreferences.HAS_SEEN_TRENDING_BANNER, false)
    }

    var hasSeenDragDismissOverlay: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(
            UserPreferences.HAS_SEEN_DRAG_DISMISS_OVERLAY,
            false
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.HAS_SEEN_DRAG_DISMISS_OVERLAY, value)
        }

    var hasSeenMerriamWebsterPermissionPane: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(
            UserPreferences.HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE,
            false
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE, value)
        }

    val hasSeenMerriamWebsterPermissionPaneLive: LiveData<Boolean> = sharedPrefs.switchMapTransform {
        PreferenceLiveData(
            it,
            UserPreferences.HAS_SEEN_MERRIAM_WEBSTER_PERMISSION_PANE,
            false
        )
    }

    var recentsListFilter: List<Period>
        get() = sharedPrefs.valueOrDefault.getPeriodList(UserPreferences.RECENTS_LIST_FILTER)
        set(value) = sharedPrefs.valueOrDefault.edit {
            putPeriodList(UserPreferences.RECENTS_LIST_FILTER, value)
        }

    val recentsListFilterLive: LiveData<List<Period>> = sharedPrefs
        .switchMapTransform {
            PreferenceLiveData<Set<String>>(
                it,
                UserPreferences.RECENTS_LIST_FILTER,
                emptySet()
            )
        }
        .mapTransform {
            it.toList().map { str -> Period.fromPrefString(str) }
        }



    // Trending list filter
    var trendingListFilter: List<Period>
        get() = sharedPrefs.valueOrDefault.getPeriodList(UserPreferences.TRENDING_LIST_FILTER)
        set(value) = sharedPrefs.valueOrDefault.edit {
            putPeriodList(UserPreferences.TRENDING_LIST_FILTER, value)
        }

    val trendingListFilterLive: LiveData<List<Period>> = sharedPrefs
        .switchMapTransform {
            PreferenceLiveData<Set<String>>(
                it,
                UserPreferences.TRENDING_LIST_FILTER,
                emptySet())
        }
        .mapTransform {
            it.toList().map { str -> Period.fromPrefString(str) }
        }

    // Favorites list filter
    var favoritesListFilter: List<Period>
        get() = sharedPrefs.valueOrDefault.getPeriodList(UserPreferences.FAVORITES_LIST_FILTER)
        set(value) = sharedPrefs.valueOrDefault.edit {
            putPeriodList(UserPreferences.FAVORITES_LIST_FILTER, value)
        }

    val favoritesListFilterLive: LiveData<List<Period>> = sharedPrefs
        .switchMapTransform {
            PreferenceLiveData<Set<String>>(
                it,
                UserPreferences.FAVORITES_LIST_FILTER,
                emptySet())

        }
        .mapTransform {
            it.toList().map { str -> Period.fromPrefString(str) }
        }

    var useTestSkus: Boolean
        get() = sharedPrefs.valueOrDefault.getBoolean(UserPreferences.USE_TEST_SKUS, false)
        set(value) = sharedPrefs.valueOrDefault.edit {
            putBoolean(UserPreferences.USE_TEST_SKUS, value)
        }
    val useTestSkusLive: LiveData<Boolean> = sharedPrefs.switchMapTransform {
        PreferenceLiveData(it, UserPreferences.USE_TEST_SKUS, false)
    }

    // portrait to landscape orientation change
    var portraitToLandscapeOrientationChangeCount: Long
        get() = sharedPrefs.valueOrDefault.getLong(
            UserPreferences.PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT,
            0L
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putLong(UserPreferences.PORTRAIT_TO_LANDSCAPE_ORIENTATION_CHANGE_COUNT, value)
        }

    // landscape to portrait orientation change
    var landscapeToPortraitOrientationChangeCount: Long
        get() = sharedPrefs.valueOrDefault.getLong(
            UserPreferences.LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT,
            0L
        )
        set(value) = sharedPrefs.valueOrDefault.edit {
            putLong(UserPreferences.LANDSCAPE_TO_PORTRAIT_ORIENTATION_CHANGE_COUNT, value)
        }


    private fun SharedPreferences.Editor.putPeriodList(
        key: String,
        value: List<Period>
    ): SharedPreferences.Editor {
        return putStringSet(key, value.map { it.prefString }.toSet())
    }

    private fun SharedPreferences.getPeriodList(
        key: String
    ): List<Period> {
        return getStringSet(key, emptySet())?.toList()?.map {
            Period.fromPrefString(it)
        } ?: emptyList()
    }
}

