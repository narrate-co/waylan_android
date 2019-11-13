package space.narrate.waylan.merriamwebster_thesaurus

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusService
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.ThesaurusEntry
import space.narrate.waylan.test_common.setBodyFromJson
import java.net.HttpURLConnection

private const val TEST_WORD = "quiescent"

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
        val response = getSynchronousResponse()

        assertThat(response.isSuccessful)
    }

    @Test
    fun getWord_shouldContainHw() {
         val response = getSynchronousResponse()

        val hw = response.body()?.map { it.hwi.hw }?.firstOrNull()
        assertThat(hw).isEqualTo("quiescent")
    }

    @Test
    fun getWord_shouldContainShortDef() {
        val response = getSynchronousResponse()

        val shortDefs = response.body()?.map { it.shortdef }?.flatten()
        assertThat(shortDefs?.contains("slow to move or act"))
    }

    @Test
    fun getWord_shouldContainMetadata() {
        val response = getSynchronousResponse()

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
    fun getWord_shouldContainDef() {
        val response = getSynchronousResponse()

        val def = response.body()?.map { it.def }?.first()?.first()?.sseq?.first()?.first()
        def?.forEach {
            println("In def, type: ${it.javaClass}, $it")
        }
    }

    private fun getSynchronousResponse(): Response<List<ThesaurusEntry>> {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBodyFromJson("quiescent.json")
        mockWebServer.enqueue(response)

        return merriamWebsterService.getWord(
            TEST_WORD,
            BuildConfig.MERRIAM_WEBSTER_THESAURUS_KEY
        ).execute()
    }
}