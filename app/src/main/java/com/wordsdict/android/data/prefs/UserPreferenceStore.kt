package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData

/**
 *
 * A top-level store for user-tied [SharedPreferences]. This makes it simpler to support multiple
 * users on a single device.
 *
 * In addition to user specific preferences, this class also provides surfacings for preferences
 * in [PreferenceStore]. [Preferences] need to be available to read outside of [UserScope],
 * but should only be written through [UserPreferenceStore]. [UserPreferences] don't need to
 * be initialized available to read or write until a valid user is available and can be both
 * read to and written from [UserPreferenceStore].
 */
class UserPreferenceStore(
        private val applicationContext: Context,
        private val preferenceStore: PreferenceStore,
        userId: String? = null
) {


    private val sharedPrefs: SharedPreferences by lazy {
        if (userId == null) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        } else {
            applicationContext.getSharedPreferences(userId, Context.MODE_PRIVATE)
        }
    }

    fun resetAll() {
        hasSeenRecentsBanner = false
        hasSeenFavoritesBanner = false
        hasSeenTrendingBanner = false
        portraitToLandscapeOrientationChangeCount = 0L
        landscapeToPortraitOrientationChangeCount = 0L
    }

    //Globally scoped preference pass-through helpers
    var orientationLock: Int
        get() = preferenceStore.orientationLock
        set(value) {
            preferenceStore.orientationLock = value
        }

    val orientationLockLive: LiveData<Orientation>
        get() = preferenceStore.orientationLive


    var nightMode: Int
        get() = preferenceStore.nightMode
        set(value) {
            preferenceStore.nightMode = value
        }

    val nightModeLive: LiveData<Int>
        get() = preferenceStore.nightModeLive


    // User scoped preferences
    // recents banner
    var hasSeenRecentsBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_RECENTS_BANNER,
            false
    )
    val hasSeenRecentsBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_RECENTS_BANNER, false)

    // favorites banner
    var hasSeenFavoritesBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_FAVORITES_BANNER,
            false
    )
    val hasSeenFavoritesBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_FAVORITES_BANNER, false)

    // trending banner
    var hasSeenTrendingBanner: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.HAS_SEEN_TRENDING_BANNER,
            false
    )
    val hasSeenTrendingBannerLive: LiveData<Boolean> =
        PreferenceLiveData(sharedPrefs, UserPreferences.HAS_SEEN_TRENDING_BANNER, false)


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

