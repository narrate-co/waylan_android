package space.narrate.waylan.android.data.mw

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import space.narrate.waylan.android.data.mw.EntryList

/**
 * Merriam-Webster [RetrofitService]
 */
interface MerriamWebsterService {
    @GET("{word}")
    fun getWord(
        @Path("word") word: String,
        @Query("key") developerKey: String
    ): Call<EntryList>
}

