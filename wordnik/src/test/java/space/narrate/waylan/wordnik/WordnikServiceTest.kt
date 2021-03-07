package space.narrate.waylan.wordnik

import java.net.HttpURLConnection
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import space.narrate.waylan.wordnik.data.remote.ApiAudio
import space.narrate.waylan.wordnik.data.remote.ApiDefinition
import space.narrate.waylan.wordnik.data.remote.ApiExamples
import space.narrate.waylan.wordnik.data.remote.ApiFrequency
import space.narrate.waylan.wordnik.data.remote.ApiHyphenation
import space.narrate.waylan.wordnik.data.remote.ApiPronunciation
import space.narrate.waylan.wordnik.data.remote.WordnikService

class WordnikServiceTest {

  private var mockServer = MockWebServer()

  private lateinit var service: WordnikService

  @Before
  fun setup() {
    mockServer.start()
    service = Retrofit.Builder()
      .baseUrl("http://api.wordnik.com/v4/")
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .create(WordnikService::class.java)
  }

  @After
  fun teardown() {
    mockServer.shutdown()
  }

  @Test
  fun definition_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.definitionQuiescent)
    )

    val response: Response<List<ApiDefinition>> = service.getDefinitions(
      "quiescent",
      BuildConfig.WORDNIK_KEY
    )
    val definitions = response.body()!!

    assertThat(definitions).isNotEmpty()
    assertThat(definitions.first().id).isEqualTo("Q5035300-1")
    assertThat(definitions.count()).isEqualTo(17)
  }

  @Test
  fun examples_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.examplesDefenestrate)
    )

    val response: Response<ApiExamples> = service.getExamples(
      "defenestrate",
      BuildConfig.WORDNIK_KEY
    )
    val entry = response.body()!!

    assertThat(entry.examples).isNotEmpty()
    assertThat(entry.examples.firstOrNull()?.provider?.get("id")).isEqualTo(711)
    assertThat(entry.examples.count()).isEqualTo(10)
  }

  @Test
  fun audio_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.audioDefenestrate)
    )

    val response: Response<List<ApiAudio>> = service.getAudio(
      "defenestrate",
      BuildConfig.WORDNIK_KEY
    )
    val entry = response.body()!!

    assertThat(entry).isNotEmpty()
    assertThat(entry.firstOrNull()?.id).isEqualTo(15850)
    assertThat(entry.firstOrNull()?.fileUrl).isNotEmpty()
  }

  @Test
  fun frequency_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.frequencyDefenestrate)
    )

    val response: Response<ApiFrequency> = service.getFrequency(
      "defenestrate",
      BuildConfig.WORDNIK_KEY
    )
    val entry = response.body()!!

    assertThat(entry.frequency).isNotEmpty()
    assertThat(entry.frequency?.firstOrNull()?.year).isEqualTo("2002")
    assertThat(entry.totalCount).isEqualTo(190)
  }

  @Test
  fun hyphenation_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.hyphenationDefenestrate)
    )

    val response: Response<List<ApiHyphenation>> = service.getHyphenation(
      "defenestrate",
      BuildConfig.WORDNIK_KEY
    )
    val entry = response.body()!!

    assertThat(entry).isNotEmpty()
    assertThat(entry.firstOrNull()?.text).isEqualTo("de")
  }

  @Test
  fun pronunciation_shouldGetValidResponse() = runBlocking {
    mockServer.enqueue(
      MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(WordnikApiResponse.pronunciationDefenestrate)
    )

    val response: Response<List<ApiPronunciation>> = service.getPronunciation(
      "defenestrate",
      BuildConfig.WORDNIK_KEY
    )
    val entry = response.body()!!

    assertThat(entry).isNotEmpty()
    assertThat(entry.firstOrNull()?.raw).isEqualTo("dē-fĕn′ĭ-strāt″")
  }
}