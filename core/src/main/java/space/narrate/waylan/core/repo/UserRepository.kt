package space.narrate.waylan.core.repo

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.firestore.FirebaseAuthWordsException
import space.narrate.waylan.core.data.firestore.FirestoreStore
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.data.prefs.PreferenceStore
import space.narrate.waylan.core.data.prefs.UserPreferenceStore
import space.narrate.waylan.core.util.switchMapTransform
import kotlin.coroutines.CoroutineContext

/**
 * A repository for all data access to all User data.
 *
 * This repository aggregates access to Firestore [User] and local User Shared Preferences
 */
class UserRepository(
    private val authenticationStore: AuthenticationStore,
    private val firestoreStore: FirestoreStore,
    private val userPreferenceStore: UserPreferenceStore,
    private val preferenceStore: PreferenceStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /** Firestore User */

    val user: LiveData<User>
        get() = authenticationStore.uid.switchMapTransform {
            firestoreStore.getUserLive(it)
        }

    /** Shared Preferences */

    val allNightModes = preferenceStore.allNightModes

    val allOrientations = preferenceStore.allOrientations

    /** Gloabl scoped Shared Preferences */

    var orientationLock: Orientation
        get() = preferenceStore.orientationLock.getValue()
        set(value) = preferenceStore.orientationLock.setValue(value)

    val orientationLockLive: LiveData<Orientation>
        get() = preferenceStore.orientationLock.getLive()

    var nightMode: NightMode
        get() = preferenceStore.nightMode.getValue()
        set(value) = preferenceStore.nightMode.setValue(value)

    val nightModeLive: LiveData<NightMode>
        get() = preferenceStore.nightMode.getLive()


    /** User scoped Shared Preferences */

    var hasSeenRecentsBanner: Boolean
        get() = userPreferenceStore.hasSeenRecentsBanner.getValue()
        set(value) = userPreferenceStore.hasSeenRecentsBanner.setValue(value)

    val hasSeenRecentsBannerLive: LiveData<Boolean>
    get() = userPreferenceStore.hasSeenRecentsBanner.getLive()

    var hasSeenFavoritesBanner: Boolean
        get() = userPreferenceStore.hasSeenFavoritesBanner.getValue()
        set(value) = userPreferenceStore.hasSeenFavoritesBanner.setValue(value)

    val hasSeenFavoritesBannerLive: LiveData<Boolean>
        get() = userPreferenceStore.hasSeenFavoritesBanner.getLive()

    var hasSeenTrendingBanner: Boolean
        get() = userPreferenceStore.hasSeenTrendingBanner.getValue()
        set(value) = userPreferenceStore.hasSeenTrendingBanner.setValue(value)

    val hasSeenTrendingBannerLive: LiveData<Boolean>
        get() = userPreferenceStore.hasSeenTrendingBanner.getLive()

    var hasSeenDragDismissOverlay: Boolean
        get() = userPreferenceStore.hasSeenDragDismissOverlay.getValue()
        set(value) = userPreferenceStore.hasSeenDragDismissOverlay.setValue(value)

    var hasSeenMerriamWebsterPermissionPane: Boolean
        get() = userPreferenceStore.hasSeenMerriamWebsterPermissionPane.getValue()
        set(value) = userPreferenceStore.hasSeenMerriamWebsterPermissionPane.setValue(value)

    val hasSeenMerriamWebsterPermissionPaneLive: LiveData<Boolean>
        get() = userPreferenceStore.hasSeenMerriamWebsterPermissionPane.getLive()

    var recentsListFilter: List<Period>
        get() = userPreferenceStore.recentsListFilter.getValue()
        set(value) = userPreferenceStore.recentsListFilter.setValue(value)

    val recentsListFilterLive: LiveData<List<Period>>
        get() = userPreferenceStore.recentsListFilter.getLive()

    var trendingListFilter: List<Period>
        get() = userPreferenceStore.trendingListFilter.getValue()
        set(value) = userPreferenceStore.trendingListFilter.setValue(value)

    val trendingListFilterLive: LiveData<List<Period>>
        get() = userPreferenceStore.trendingListFilter.getLive()

    var favoritesListFilter: List<Period>
        get() = userPreferenceStore.favoritesListFilter.getValue()
        set(value) = userPreferenceStore.favoritesListFilter.setValue(value)

    val favoritesListFilterLive: LiveData<List<Period>>
        get() = userPreferenceStore.favoritesListFilter.getLive()

    var useTestSkus: Boolean
        get() = userPreferenceStore.useTestSkus.getValue()
        set(value) = userPreferenceStore.useTestSkus.setValue(value)

    val useTestSkusLive: LiveData<Boolean>
        get() = userPreferenceStore.useTestSkus.getLive()

    var portraitToLandscapeOrientationChangeCount: Long
        get() = userPreferenceStore.portraitToLandscapeOrientationChangeCount.getValue()
        set(value) = userPreferenceStore.portraitToLandscapeOrientationChangeCount.setValue(value)

    var landscapeToPortraitOrientationChangeCount: Long
        get() = userPreferenceStore.landscapeToPortraitOrientationChangeCount.getValue()
        set(value) = userPreferenceStore.landscapeToPortraitOrientationChangeCount.setValue(value)


    suspend fun getUser(): Result<User> {
        val uid = authenticationStore.uid.value
            ?: return Result.Error(FirebaseAuthWordsException.NoCurrentUserException)

        return firestoreStore.getUser(uid)
    }

    fun setUserMerriamWebsterState(state: PluginState) {
        val uid = authenticationStore.uid.value ?: return

        // Launch and forget
        launch {
            userPreferenceStore.hasSeenMerriamWebsterPermissionPane.setValue(false)
            firestoreStore.setUserMerriamWebsterState(uid, state)
        }
    }

    fun resetPreferences() {
        preferenceStore.resetAll()
        userPreferenceStore.resetAll()
    }

}