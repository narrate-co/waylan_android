package space.narrate.waylan.wordnik.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * A RetrofitService singleton that creates [WordnikService]
 */
object RetrofitService {
    private var instance: WordnikService? = null

    fun getInstance(): WordnikService {
        if (instance == null)  {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://api.wordnik.com/v4/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            instance = retrofit.create(WordnikService::class.java)
        }

        return instance!!
    }
}
