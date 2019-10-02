package space.narrate.waylan.merriamwebster

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import space.narrate.waylan.merriamwebster.data.remote.MerriamWebsterStore
import space.narrate.waylan.test_common.CoroutinesTestRule
import org.mockito.Mockito.`when` as whenever

private const val QUERY = "quiescent"

@ExperimentalCoroutinesApi
class MerriamWebsterStoreTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    // Mock Android's getMainLooper()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val merriamWebsterService = mock(MerriamWebsterService::class.java)
    private val mwDao = mock(MwDao::class.java)

    private lateinit var merriamWebsterStore: MerriamWebsterStore

    @Before
    fun setUp() {
        merriamWebsterStore = MerriamWebsterStore(merriamWebsterService, mwDao)
    }

    @Test
    fun get_shouldCallApiIfNoLocalEntry() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val call: Call<EntryList> = mock(Call::class.java) as Call<EntryList>

        whenever(mwDao.getDefinitions(QUERY)).thenReturn(emptyList())
        whenever(merriamWebsterService.getWord(anyString(), anyString())).thenReturn(call)

        merriamWebsterStore.getWordAndDefinitions(QUERY)

        verify(merriamWebsterService).getWord(anyString(), anyString())
    }
}