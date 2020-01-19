package space.narrate.waylan.merriamwebster

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.Call
import space.narrate.waylan.merriamwebster.data.local.MwDao
import space.narrate.waylan.merriamwebster.data.remote.EntryList
import space.narrate.waylan.merriamwebster.data.remote.MerriamWebsterService
import space.narrate.waylan.merriamwebster.data.MerriamWebsterStore
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.valueBlocking
import org.mockito.Mockito.`when` as whenever

private const val QUERY = "quiescent"

@ExperimentalCoroutinesApi
class MerriamWebsterStoreTest {

    // Mock Android's getMainLooper(), forcing Architecture components to run synchronously
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val merriamWebsterService = mock(MerriamWebsterService::class.java)
    private val mwDao = mock(MwDao::class.java)

    private val testCoroutinesDispatcher = TestCoroutineDispatcher()

    private lateinit var merriamWebsterStore: MerriamWebsterStore

    @Before
    fun setUp() {
        merriamWebsterStore = MerriamWebsterStore(merriamWebsterService, mwDao, testCoroutinesDispatcher)
    }

    @Test
    fun get_shouldCallApiIfNoLocalEntry() = testCoroutinesDispatcher.runBlockingTest {
        val call: Call<EntryList> = mock(Call::class.java) as Call<EntryList>

        whenever(mwDao.getDefinitions(QUERY)).thenReturn(emptyList())
        whenever(merriamWebsterService.getWord(anyString(), anyString())).thenReturn(call)

        testCoroutinesDispatcher.pauseDispatcher()
        merriamWebsterStore.getWordAndDefinitions(QUERY)

        testCoroutinesDispatcher.resumeDispatcher()
        verify(merriamWebsterService).getWord(anyString(), anyString())
    }
}