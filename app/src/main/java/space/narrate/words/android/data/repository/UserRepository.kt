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
import space.narrate.words.android.util.LiveDataUtils
import kotlinx.coroutines.launch
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.prefs.NightMode
import space.narrate.words.android.data.prefs.ThirdPartyLibrary
import space.narrate.words.android.data.prefs.ThirdPartyLibraryStore
import space.narrate.words.android.ui.settings.NightModeRadioItemModel
import space.narrate.words.android.ui.settings.OrientationRadioItemModel
import space.narrate.words.android.util.switchMapTransform
import kotlin.coroutines.CoroutineContext

/**
 * A repository for all data access to all User data.
 *
 * This repository aggregates access to Firestore [User] and local User Shared Preferences
 *
 */
class UserRepository(
    private val authenticationStore: AuthenticationStore,
    private val firestoreStore: FirestoreStore,
    private val userPreferenceStore: UserPreferenceStore,
    private val preferenceStore: PreferenceStore,
    private val thirdPartyLibraryStore: ThirdPartyLibraryStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /** Firestore User */

    val user: LiveData<User>
        get() = authenticationStore.user
            .switchMapTransform { firestoreStore.getUserLive(it.uid) }

    /** Shared Preferences */

    val allNightModes = preferenceStore.allNightModes

    val allOrientations = preferenceStore.allOrientations

    /** Gloabl scoped Shared Preferences */

    var orientationLock: Orientation
        get() = preferenceStore.orientationLock
        set(value) {
            preferenceStore.orientationLock = value
        }

    val orientationLockLive: LiveData<Orientation> = preferenceStore.orientationLockLive

    var nightMode: NightMode
        get() = preferenceStore.nightMode
        set(value) {
            preferenceStore.nightMode = value
        }

    val nightModeLive: LiveData<NightMode> = preferenceStore.nightModeLive


    /** User scoped Shared Preferences */

    var hasSeenRecentsBanner: Boolean
        get() = userPreferenceStore.hasSeenRecentsBanner
        set(value) {
            userPreferenceStore.hasSeenRecentsBanner = value
        }

    val hasSeenRecentsBannerLive: LiveData<Boolean> =
        userPreferenceStore.hasSeenRecentsBannerLive

    var hasSeenFavoritesBanner: Boolean
        get() = userPreferenceStore.hasSeenFavoritesBanner
        set(value) {
            userPreferenceStore.hasSeenFavoritesBanner = value
        }

    val hasSeenFavoritesBannerLive: LiveData<Boolean> =
        userPreferenceStore.hasSeenFavoritesBannerLive

    var hasSeenTrendingBanner: Boolean
        get() = userPreferenceStore.hasSeenTrendingBanner
        set(value) {
            userPreferenceStore.hasSeenTrendingBanner = value
        }

    val hasSeenTrendingBannerLive: LiveData<Boolean> =
        userPreferenceStore.hasSeenTrendingBannerLive

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

    val hasSeenMerriamWebsterPermissionPaneLive: LiveData<Boolean> =
        userPreferenceStore.hasSeenMerriamWebsterPermissionPaneLive

    var recentsListFilter: List<Period>
        get() = userPreferenceStore.recentsListFilter
        set(value) {
          userPreferenceStore.recentsListFilter = value
        }

    val recentsListFilterLive: LiveData<List<Period>> = userPreferenceStore.recentsListFilterLive

    var trendingListFilter: List<Period>
        get() = userPreferenceStore.trendingListFilter
        set(value) {
            userPreferenceStore.trendingListFilter = value
        }

    val trendingListFilterLive: LiveData<List<Period>> = userPreferenceStore.trendingListFilterLive

    var favoritesListFilter: List<Period>
        get() = userPreferenceStore.favoritesListFilter
        set(value) {
            userPreferenceStore.favoritesListFilter = value
        }

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

    val allThirdPartyLibraries: List<ThirdPartyLibrary> = thirdPartyLibraryStore.all

    fun setUserMerriamWebsterState(state: PluginState) {
        val uid = authenticationStore.uid ?: return

        launch {
            userPreferenceStore.hasSeenMerriamWebsterPermissionPane = false
            firestoreStore.setUserMerriamWebsterState(uid, state)
        }
    }

    fun resetPreferences() {
        preferenceStore.resetAll()
        userPreferenceStore.resetAll()
    }

}