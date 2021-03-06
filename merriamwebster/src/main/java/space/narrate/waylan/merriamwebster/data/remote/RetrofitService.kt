package space.narrate.waylan.merriamwebster.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

/**
 * A RetrofitService singleton that creates [MerriamWebsterService]
 */
object RetrofitService {
    private var instance: MerriamWebsterService? = null

    fun getInstance(): MerriamWebsterService {
        if (instance == null)  {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                )
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(
                    "https://www.dictionaryapi.com/api/v1/references/collegiate/xml/"
                )
                .client(okHttpClient)
                .addConverterFactory(
                    SimpleXmlConverterFactory.create(Persister(AnnotationStrategy()))
                )
                .build()

            instance = retrofit.create(MerriamWebsterService::class.java)
        }

        return instance!!
    }
}
