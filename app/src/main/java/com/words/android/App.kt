package com.words.android

import android.app.Application
import com.words.android.data.disk.AppDatabase
import com.words.android.data.repository.WordRepository

class App: Application() {

    lateinit var wordRepository: WordRepository

    val viewModelFactory: ViewModelFactory by lazy { ViewModelFactory(this) }

    override fun onCreate() {
        super.onCreate()
        wordRepository = WordRepository(AppDatabase.getInstance(this))
    }


}

