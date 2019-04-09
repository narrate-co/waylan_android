package space.narrate.words.android.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.prefs.UserPreferenceStore
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.LiveDataHelper
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A repository for all data access to all User data.
 *
 * This repository aggregates access to Firestore [User] and local User Shared Preferences
 *
 */
class UserRepository(
        private val firestoreStore: FirestoreStore?,
        private val userPreferenceStore: UserPreferenceStore,
        private val preferenceStore: PreferenceStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /** Firestore User */

    fun getUser(): LiveData<User> =
        firestoreStore?.getUserLive() ?: LiveDataHelper.empty()

    fun setUserMerriamWebsterState(state: PluginState) {
        launch {
            userPreferenceStore.hasSeenMerriamWebsterPermissionPane = false
            firestoreStore?.setUserMerriamWebsterState(state)
        }
    }

    /** Shared Preferences */

    fun resetPreferences() {
        preferenceStore.resetAll()
        userPreferenceStore.resetAll()
    }


    /** Gloabl scoped Shared Preferences */

    var orientationLock: Int
        get() = preferenceStore.orientationLock
        set(value) {
            preferenceStore.orientationLock = value
        }

    val orientationLockLive: LiveData<Orientation> = preferenceStore.orientationLive

    var nightMode: Int
        get() = preferenceStore.nightMode
        set(value) {
            preferenceStore.nightMode = value
        }

    val nightModeLive: LiveData<Int> = preferenceStore.nightModeLive


    /** User scoped Shared Preferences */

    var hasSeenRecentsBanner: Boolean
        get() = userPreferenceStore.hasSeenRecentsBanner
        set(value) {
            userPreferenceStore.hasSeenRecentsBanner = value
        }

    var hasSeenFavoritesBanner: Boolean
        get() = userPreferenceStore.hasSeenFavoritesBanner
        set(value) {
            userPreferenceStore.hasSeenFavoritesBanner = value
        }

    var hasSeenTrendingBanner: Boolean
        get() = userPreferenceStore.hasSeenTrendingBanner
        set(value) {
            userPreferenceStore.hasSeenTrendingBanner = value
        }

    var hasSeenDragDismissOverlay: Boolean
        get() = userPreferenceStore.hasSeenDragDismissOverlay
        set(value) {
            userPreferenceStore.hasSeenDragDismissOverlay = value
        }

    var hasSeenMerriamWebsterPermissionPane: Boolean
        get() = userPreferenceStore.hasSeenMerriamWebsterPermissionPane
        set(value) {
            userPreferenceStore.hasSeenMerriamWebsterPermissionPane = value
        }

    var recentsListFilter: List<Period>
        get() = userPreferenceStore.getRecentsListFilter()
        set(value) = userPreferenceStore.setRecentsListFilter(value)

    val recentsListFilterLive: LiveData<List<Period>> = userPreferenceStore.recentsListFilterLive

    var trendingListFilter: List<Period>
        get() = userPreferenceStore.getTrendingListFilter()
        set(value) = userPreferenceStore.setTrendingListFilter(value)

    val trendingListFilterLive: LiveData<List<Period>> = userPreferenceStore.trendingListFilterLive

    var favoritesListFilter: List<Period>
        get() = userPreferenceStore.getFavoritesListFilter()
        set(value) = userPreferenceStore.setFavoritesListFilter(value)

    val favoritesListFilterLive: LiveData<List<Period>> =
            userPreferenceStore.favoritesListFilterLive

    var useTestSkus: Boolean
        get() = userPreferenceStore.useTestSkus
        set(value) {
            userPreferenceStore.useTestSkus = value
        }

    val useTestSkusLive: LiveData<Boolean> = userPreferenceStore.useTestSkusLive

    var portraitToLandscapeOrientationChangeCount: Long
        get() = userPreferenceStore.portraitToLandscapeOrientationChangeCount
        set(value) {
            userPreferenceStore.portraitToLandscapeOrientationChangeCount = value
        }

    var landscapeToPortraitOrientationChangeCount: Long
        get() = userPreferenceStore.landscapeToPortraitOrientationChangeCount
        set(value) {
            userPreferenceStore.landscapeToPortraitOrientationChangeCount
        }

}