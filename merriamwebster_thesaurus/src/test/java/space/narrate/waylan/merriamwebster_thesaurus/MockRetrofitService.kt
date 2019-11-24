package space.narrate.waylan.merriamwebster_thesaurus

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.EntryAdapter
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusService
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.RetrofitService

object MockRetrofitService {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        )
        .build()


    fun getInstance(mockWebServer: MockWebServer): MerriamWebsterThesaurusService {
        val moshi = Moshi.Builder()
            .add(EntryAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MerriamWebsterThesaurusService::class.java)
    }
}