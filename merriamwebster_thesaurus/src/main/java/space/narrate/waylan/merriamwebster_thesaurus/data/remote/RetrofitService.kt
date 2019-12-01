package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import androidx.annotation.VisibleForTesting
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * A RetrofitService singleton that creates [MerriamWebsterThesaurusService].
 *
 * TODO: Expose a moshi builder to be reused in testing.
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
            val moshi = Moshi.Builder()
                .add(EntryAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(
                    "https://www.dictionaryapi.com/api/v3/references/thesaurus/json/"
                )
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            instance = retrofit.create(MerriamWebsterThesaurusService::class.java)
        }

        return instance!!
    }
}
