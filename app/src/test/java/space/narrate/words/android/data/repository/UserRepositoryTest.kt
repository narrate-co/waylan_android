package space.narrate.words.android.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations.initMocks
import space.narrate.words.android.CoroutinesTestRule
import org.mockito.Mockito.`when` as whenever
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.users.UserWordType
import space.narrate.words.android.data.mw.MerriamWebsterStore
import space.narrate.words.android.data.spell.SymSpellStore
import space.narrate.words.android.LiveDataTestUtils
import space.narrate.words.android.data.Result
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.prefs.Preference
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.prefs.ThirdPartyLibraryStore
import space.narrate.words.android.data.prefs.UserPreferenceStore
import space.narrate.words.android.testDatabase
import space.narrate.words.android.util.LiveDataUtils
import space.narrate.words.android.valueBlocking

@ExperimentalCoroutinesApi
class UserRepositoryTest {

    private val authenticationStore = mock(AuthenticationStore::class.java)
    private val firestoreStore = mock(FirestoreStore::class.java)
    private val userPreferenceStore = mock(UserPreferenceStore::class.java)
    private val preferenceStore = mock(PreferenceStore::class.java)
    private val thirdPartyLibraryStore = mock(ThirdPartyLibraryStore::class.java)

    private val user: MutableLiveData<User> = MutableLiveData()
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
        user.value = testDatabase.users[0].user
        user1Word.value = testDatabase.users[0].userWords[0]
        user2Word.value = testDatabase.users[1].userWords[1]

        userRepository = UserRepository(
            authenticationStore,
            firestoreStore,
            userPreferenceStore,
            preferenceStore,
            thirdPartyLibraryStore
        )
    }

    @Test
    fun setUserMerriamWebsterState_shouldResetHasSeenPermissionPane_shouldCallFirestore() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            whenever(authenticationStore.user).thenReturn(user)

            val preference = mock(Preference::class.java)
            whenever(userPreferenceStore.hasSeenMerriamWebsterPermissionPane)
                .thenReturn(preference as Preference<Boolean>)


            val state = PluginState.None()
            userRepository.setUserMerriamWebsterState(state)

            verify(userPreferenceStore.hasSeenMerriamWebsterPermissionPane).setValue(false)
            verify(firestoreStore).setUserMerriamWebsterState(user.value!!.uid, state)
        }
}