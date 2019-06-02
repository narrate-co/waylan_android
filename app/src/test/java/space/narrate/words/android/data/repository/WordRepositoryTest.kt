package space.narrate.words.android.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
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
import space.narrate.words.android.valueBlocking

class WordRepositoryTest {

    // Mock AppDatabase
    private val db = mock(AppDatabase::class.java)
    // Mock AuthenticationStore
    private val authenticationStore = mock(AuthenticationStore::class.java)
    // Mock FirestoreStore
    private val firestoreStore = mock(FirestoreStore::class.java)
    // Mock MerriamWebsterStore
    private val merriamWebsterStore = mock(MerriamWebsterStore::class.java)
    // Mock SymSpellStore
    private val symSpellStore = mock(SymSpellStore::class.java)

    private val user: MutableLiveData<User> = MutableLiveData()
    private val user1Word: MutableLiveData<UserWord> = MutableLiveData()
    private val user2Word: MutableLiveData<UserWord> = MutableLiveData()

    private lateinit var wordRepository: WordRepository

    // Mock Android's getMainLooper()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        user1Word.value = UserWord(
            "123",
            "quiescent",
            types = mutableMapOf(UserWordType.FAVORITED.name to true)
        )
        user2Word.value = UserWord(
            "123",
            "quiescent",
            types = mutableMapOf(UserWordType.FAVORITED.name to false)
        )

        wordRepository = WordRepository(
            db,
            authenticationStore,
            firestoreStore,
            merriamWebsterStore,
            symSpellStore
        )
    }

    @Test
    fun getUserWord_shouldReturnWhenAuthenticated() {
        whenever(authenticationStore.uid).thenReturn("abc")
        whenever(firestoreStore.getUserWordLive("123", "abc")).thenReturn(user1Word)

        assertThat(wordRepository.getUserWord("123").value).isEqualTo(user1Word.value)
    }

    // Test get UserWord unauthenticated. Authenticating after observing
    // should update value.
    @Test
    fun getUserWord_shouldChangeWhenAuthenticationChanges() {
        whenever(authenticationStore.uid).thenReturn("abc")

        whenever(firestoreStore.getUserWordLive("123", "abc")).thenReturn(user1Word)
        whenever(firestoreStore.getUserWordLive("123", "bcd")).thenReturn(user2Word)

        // set authentication to user 1
        user.value = User("abc")

        // observe user word 123
        val result = wordRepository.getUserWord("123")

        // verify user word 123 is user 1's
        assertThat(result.valueBlocking).isEqualTo(user1Word.value)

        // set authentication to user 2
        user.value = User("bcd")

        // verify user word 123 is user 2's
        assertThat(result.valueBlocking).isEqualTo(user2Word.value)
    }
}