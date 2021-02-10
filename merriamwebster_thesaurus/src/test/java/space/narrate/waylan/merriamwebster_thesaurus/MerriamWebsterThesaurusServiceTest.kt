package space.narrate.waylan.merriamwebster_thesaurus

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusService
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.RemoteThesaurusEntry
import space.narrate.waylan.test_common.setBodyFromJsonResource
import java.net.HttpURLConnection

private const val QUIESCENT = "quiescent"
private const val RENDEZVOUS = "rendezvous"

class MerriamWebsterThesaurusServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var merriamWebsterService: MerriamWebsterThesaurusService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        merriamWebsterService = MockRetrofitService.getInstance(mockWebServer)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getWord_shouldReturnSuccessfully() {
        val responseQuiescent = getSynchronousResponse(QUIESCENT)
        val responseRendezvous = getSynchronousResponse(RENDEZVOUS)

        assertThat(responseQuiescent.isSuccessful)
        assertThat(responseRendezvous.isSuccessful)
    }

    @Test
    fun getWord_shouldContainHw() {
         val response = getSynchronousResponse(QUIESCENT)

        val hw = response.body()?.map { it.hwi.hw }?.firstOrNull()
        assertThat(hw).isEqualTo("quiescent")
    }

    @Test
    fun getWord_shouldContainShortDef() {
        val response = getSynchronousResponse(QUIESCENT)

        val shortDefs = response.body()?.map { it.shortdef }?.flatten()
        assertThat(shortDefs?.contains("slow to move or act"))
    }

    @Test
    fun getWord_shouldContainMetadata() {
        val response = getSynchronousResponse(QUIESCENT)

        val meta = response.body()?.map { it.meta }?.firstOrNull()!!

        assertThat(meta.id).isEqualTo("quiescent")
        assertThat(meta.section).isEqualTo("alpha")
        assertThat(meta.stems).containsExactly("quiescent", "quiescently")
        assertThat(meta.offensive).isFalse()
        assertThat(meta.syns.flatten()).containsExactly(
            "dull",
            "inactive",
            "inert",
            "lethargic",
            "sleepy",
            "sluggish",
            "torpid"
        )
        assertThat(meta.ants.flatten()).containsExactly("active")
    }

    @Test
    fun getQuiescent_shouldContainDef() {
        val response = getSynchronousResponse(QUIESCENT)

        val allEntries = response.body()?.map { it.def }?.map { it.entries }?.flatten()!!

        // Contains synonyms
        assertThat(
            allEntries.map { it.syn_list }.flatten().flatten().map { it.wd }
        ).containsExactly(
            "dull",
            "inactive",
            "inert",
            "lethargic",
            "sleepy",
            "sluggish",
            "torpid"
        )

        // Contains related words
        assertThat(
            allEntries.map { it.rel_list }.flatten().flatten().map { it.wd }
        ).containsAtLeastElementsIn(listOf(
            "ambitionless",
            "apathetic",
            "indolent",
            "languorous",
            "lazy",
            "lazyish",
            "listless",
            "shiftless",
            "slack",
            "slothful",
            "sluggardly",
            "dormant",
            "dead"
        ))

        // Contains near words
        assertThat(
            allEntries.map { it.near_list }.flatten().flatten().map { it.wd }
        ).containsAtLeastElementsIn(listOf(
            "busy",
            "working",
            "animated",
            "dynamic",
            "assiduous",
            "sedulous"
        ))

        // Contains antonyms
        assertThat(
            allEntries.map { it.ant_list }.flatten().flatten().map { it.wd }
        ).containsAtLeastElementsIn(listOf(
            "active"
        ))
    }

    @Test
    fun getRendezvous_shouldContainDef() {
        val response = getSynchronousResponse(RENDEZVOUS)

        val allEntries = response.body()?.map { it.def }?.map { it.entries }?.flatten()!!

        assertThat(
            allEntries.map { it.syn_list }.flatten().flatten().map { it.wd }
        ).containsAtLeastElementsIn(listOf(
            "hangout",
            "purlieu",
            "resort",
            "appointment",
            "date",
            "tryst",
            "cluster",
            "convene"
        ))
    }

    @Test
    fun getWord_shouldHaveShortDef() {
        val response = getSynchronousResponse(QUIESCENT)

        assertThat(response.body()?.first()?.shortdef?.first())
            .isEqualTo("slow to move or act")
    }

    private fun getSynchronousResponse(word: String): Response<List<RemoteThesaurusEntry>> {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBodyFromJsonResource("$word.json")

        mockWebServer.enqueue(response)

        return merriamWebsterService.getWord(
            word,
            BuildConfig.MERRIAM_WEBSTER_THESAURUS_KEY
        ).execute()
    }
}