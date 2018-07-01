package com.words.android.data.mw

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MerriamWebsterService {
    @GET("{word}")
    fun getWord(@Path("word") word: String, @Query("key") developerKey: String): Call<MwApiWord>
}

