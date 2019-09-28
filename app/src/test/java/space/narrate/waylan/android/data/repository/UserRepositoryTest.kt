package space.narrate.waylan.android.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import space.narrate.waylan.android.CoroutinesTestRule
import space.narrate.waylan.android.data.auth.AuthenticationStore
import space.narrate.waylan.android.data.firestore.FirestoreStore
import space.narrate.waylan.android.data.firestore.users.PluginState
import space.narrate.waylan.android.data.firestore.users.UserWord
import space.narrate.waylan.android.data.prefs.Preference
import space.narrate.waylan.android.data.prefs.PreferenceStore
import space.narrate.waylan.android.data.prefs.UserPreferenceStore
import space.narrate.waylan.android.testDatabase
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
class UserRepositoryTest {

    private val authenticationStore = mock(AuthenticationStore::class.java)
    private val firestoreStore = mock(FirestoreStore::class.java)
    private val userPreferenceStore = mock(UserPreferenceStore::class.java)
    private val preferenceStore = mock(PreferenceStore::class.java)

    private val uid: MutableLiveData<String> = MutableLiveData()
    private val user1Word: MutableLiveData<UserWord> = MutableLiveData()
    private val user2Word: MutableLiveData<UserWord> = MutableLiveData()

    private lateinit var userRepository: UserRepository

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    // Mock Android's getMainLooper()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        uid.value = testDatabase.users[0].user.uid
        user1Word.value = testDatabase.users[0].userWords[0]
        user2Word.value = testDatabase.users[1].userWords[1]

        userRepository = UserRepository(
            authenticationStore,
            firestoreStore,
            userPreferenceStore,
            preferenceStore
        )
    }

    // TODO: This test is flakey and should be fixed or removed.
    @Test
    fun setUserMerriamWebsterState_shouldResetHasSeenPermissionPane_shouldCallFirestore() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            whenever(authenticationStore.uid).thenReturn(uid)

            val preference = mock(Preference::class.java)
            whenever(userPreferenceStore.hasSeenMerriamWebsterPermissionPane)
                .thenReturn(preference as Preference<Boolean>)


            val state = PluginState.None()
            userRepository.setUserMerriamWebsterState(state)

            verify(userPreferenceStore.hasSeenMerriamWebsterPermissionPane).setValue(false)
            verify(firestoreStore).setUserMerriamWebsterState(uid.value!!, state)
        }
}