package space.narrate.waylan.merriamwebster

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import space.narrate.waylan.merriamwebster.data.MerriamWebsterRepository
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups
import space.narrate.waylan.merriamwebster.data.remote.MerriamWebsterStore
import org.mockito.Mockito.`when` as whenever

class MerriamWebsterRepositoryTest {

    private val merriamWebsterStore = mock(MerriamWebsterStore::class.java)

    private lateinit var merriamWebsterRepository: MerriamWebsterRepository

    @Before
    fun setUp() {
        merriamWebsterRepository = MerriamWebsterRepository(merriamWebsterStore)
    }

    @Test
    fun shouldReturnLiveData() {
        val query = "quiescent"
        val result = MutableLiveData<List<MwWordAndDefinitionGroups>>()
        whenever(merriamWebsterStore.getWordAndDefinitions(query)).thenReturn(result)

        assertThat(merriamWebsterRepository.getMerriamWebsterWord(query))
            .isInstanceOf(LiveData::class.java)
    }
}