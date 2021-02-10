package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Merriam-Webster thesaurus [RetrofitService].
 */
interface MerriamWebsterThesaurusService {
    @GET("{word}")
    fun getWord(
        @Path("word") word: String,
        @Query("key") developerKey: String
    ): Call<List<RemoteThesaurusEntry>>
}

