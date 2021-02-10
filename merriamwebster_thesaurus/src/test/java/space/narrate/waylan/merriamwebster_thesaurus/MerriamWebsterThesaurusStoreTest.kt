package space.narrate.waylan.merriamwebster_thesaurus

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.Def
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.Entry
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.Hwi
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusService
import space.narrate.waylan.merriamwebster_thesaurus.data.MerriamWebsterThesaurusStore
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusDao
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.Meta
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.RemoteThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.Wd
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.toLocalThesaurusEntry
import space.narrate.waylan.test_common.toSuccessfulCall
import space.narrate.waylan.test_common.valueBlocking
import org.mockito.Mockito.`when` as whenever

private const val TEST_WORD = "quiescent"

class MerriamWebsterThesaurusStoreTest {

    private val merriamWebsterThesaurusService = mock(MerriamWebsterThesaurusService::class.java)
    private val thesaurusDao = mock(ThesaurusDao::class.java)
    private lateinit var merriamWebsterThesaurusStore: MerriamWebsterThesaurusStore

    // Mock Android's getMainLooper()
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        merriamWebsterThesaurusStore = MerriamWebsterThesaurusStore(
            merriamWebsterThesaurusService,
            thesaurusDao
        )
        val c =
        whenever(merriamWebsterThesaurusService.getWord(
            TEST_WORD,
            BuildConfig.MERRIAM_WEBSTER_THESAURUS_KEY
        )).thenReturn(getTestWordCall().toSuccessfulCall())
    }

    @Test
    fun remoteThesaurusEntry_isConvertedToLocalThesaurusEntry() {
        val response = getTestWordCall()

        val entry = response.first().toLocalThesaurusEntry
        assertThat(entry.id).isEqualTo("123")
        assertThat(entry.word).isEqualTo("quiescent")
        assertThat(entry.src).isEqualTo("Merriam-Webster")
        assertThat(entry.synonymWords).containsAtLeastElementsIn(listOf(
            "dull", "inactive", "inert"
        ))
        assertThat(entry.relatedWords).containsAtLeastElementsIn(listOf(
            "ambitionless", "apathetic", "indolent"
        ))
        assertThat(entry.nearWords).containsAtLeastElementsIn(listOf(
            "busy", "engaged", "occupied"
        ))
        assertThat(entry.antonymWords).containsAtLeastElementsIn(listOf(
            "active"
        ))
    }

    private fun getTestWordCall(): List<RemoteThesaurusEntry> =
        listOf(
            RemoteThesaurusEntry(
                Meta(
                    "123",
                    "abc",
                    "Merriam-Webster",
                    "alpha",
                    listOf("quiescent", "quiescently"),
                    listOf(listOf("dull", "inactive", "inert")),
                    listOf(listOf("active")),
                    false
                ),
                Hwi("quiescent"),
                "adjective",
                Def(
                    listOf(
                        Entry(
                            "1",
                            Any(),
                            listOf(listOf(Wd("dull"), Wd("inactive"), Wd("inert"))),
                            listOf(listOf(Wd("ambitionless"), Wd("apathetic"), Wd("indolent"))),
                            listOf(listOf(Wd("busy"), Wd("engaged"), Wd("occupied"))),
                            listOf(listOf(Wd("active")))
                        )
                    )
                ),
                listOf("slow to move or act")
            )
        )
}