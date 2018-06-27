package com.words.android

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.words.android.ui.search.SearchViewModel

class ViewModelFactory(val app: App): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(app.wordRepository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(app.wordRepository) as T
            else -> throw IllegalArgumentException("No matching ViewModel class")
        }
    }
}

