package space.narrate.waylan.android.data.prefs

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import org.robolectric.RobolectricTestRunner
import space.narrate.waylan.android.data.auth.AuthenticationStore
import space.narrate.waylan.android.data.firestore.users.User

@RunWith(RobolectricTestRunner::class)
class UserPreferenceStoreTest : KoinTest {

    private val user: MutableLiveData<User> = MutableLiveData()

    private val authenticationStore = mock(AuthenticationStore::class.java)

    private lateinit var userPreferenceStore: UserPreferenceStore

    @Before
    fun setUp() {
        whenever(authenticationStore.user).thenReturn(user)
        userPreferenceStore = UserPreferenceStore(
            ApplicationProvider.getApplicationContext(),
            authenticationStore
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun hasSeenRecentsBanner_shouldReturnDefaultValue() {
        assertThat(userPreferenceStore.hasSeenRecentsBanner.getValue()).isFalse()
    }

    @Test
    fun hasSeenDragDismissOverlay_persistsAcrossUserChanges() {
        // No user is set. UPS should be using default shared preferences
        assertThat(userPreferenceStore.hasSeenDragDismissOverlay.getValue()).isFalse()

        // User 1 is set. UPS should switch to using shared preferences for user 1
        user.value = User("USER1")
        userPreferenceStore.hasSeenDragDismissOverlay.setValue(true)
        assertThat(userPreferenceStore.hasSeenDragDismissOverlay.getValue()).isTrue()

        // User 2 is set. UPS should switch to using shared preferences for user 2
        // Ensure User 2 is given the default preference value of false.
        user.value = User("USER2")
        assertThat(userPreferenceStore.hasSeenDragDismissOverlay.getValue()).isFalse()

        // User 1 is set. UPS should switch to using shared preferences for user 2
        // Ensure that switching back to user 1 recalls our previous value of 'true'.
        user.value = User("USER1")
        assertThat(userPreferenceStore.hasSeenDragDismissOverlay.getValue()).isTrue()
    }

}