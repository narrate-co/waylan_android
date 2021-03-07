package space.narrate.waylan.wordnik.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Wordnik [RetrofitService]
 */
interface WordnikService {
  /**
   * Get all definitions from all dictionaries for a word.
   * Example query:
   * http://api.wordnik.com/v4/word.json/quiescent/definitions?api_key=XYZ
   */
  @GET("word.json/{word}/definitions")
  suspend fun getDefinitions(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<List<ApiDefinition>>

  @GET("word.json/{word}/examples")
  suspend fun getExamples(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<ApiExamples>

  @GET("word.json/{word}/audio")
  suspend fun getAudio(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<List<ApiAudio>>

  @GET("word.json/{word}/frequency")
  suspend fun getFrequency(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<ApiFrequency>

  @GET("word.json/{word}/hyphenation")
  suspend fun getHyphenation(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<List<ApiHyphenation>>

  @GET("word.json/{word}/pronunciations")
  suspend fun getPronunciation(
    @Path("word") word: String,
    @Query("api_key") key: String
  ): Response<List<ApiPronunciation>>
}