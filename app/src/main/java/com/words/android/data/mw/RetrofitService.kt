package com.words.android.data.mw

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitService {
    private var instance: MerriamWebsterService? = null

    fun getInstance(): MerriamWebsterService {
        if (instance == null)  {
            //init
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.dictionaryapi.com/api/v1/references/collegiate/xml/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
            instance = retrofit.create(MerriamWebsterService::class.java)
        }

        return instance!!
    }
}
