package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData

class UserPreferenceRepository(
        private val applicationContext: Context,
        private val preferenceRepository: PreferenceRepository,
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
        get() = preferenceRepository.orientationLock
        set(value) {
            preferenceRepository.orientationLock = value
        }

    val orientationLockLive: LiveData<Orientation>
        get() = preferenceRepository.orientationLive


    var nightMode: Int
        get() = preferenceRepository.nightMode
        set(value) {
            preferenceRepository.nightMode = value
        }

    val nightModeLive: LiveData<Int>
        get() = preferenceRepository.nightModeLive


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

    // able to suggest orientation unlock
    // this should only be true when
    //// A. The app orientation is locked
    //// B. The app has just opened
    //// C. Patterns matched is high/they're rotating a lot - (PATTERNS_MATCHED_AFTER_LAST_LOCK_OR_OPEN > n)

    //// D. Multiple patterns are matched in very quick succession (should be handled by RotationManager)
    var isAbleToSuggestOrientationUnlock: Boolean by PreferenceDelegate(
            sharedPrefs,
            UserPreferences.IS_ABLE_TO_SUGGEST_ORIENTATION_UNLOCK,
            false
    )


}

