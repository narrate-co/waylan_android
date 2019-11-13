package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * A RetrofitService singleton that creates [MerriamWebsterService]
 */
object RetrofitService {
    private var instance: MerriamWebsterThesaurusService? = null

    fun getInstance(): MerriamWebsterThesaurusService {
        if (instance == null)  {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(
                    "https://www.dictionaryapi.com/api/v3/references/thesaurus/json/"
                )
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            instance = retrofit.create(MerriamWebsterThesaurusService::class.java)
        }

        return instance!!
    }
}
