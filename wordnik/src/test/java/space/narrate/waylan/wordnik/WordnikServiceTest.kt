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
import space.narrate.waylan.wordnik.data.remote.ApiDefinition
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
  fun testResponse() = runBlocking {
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
}