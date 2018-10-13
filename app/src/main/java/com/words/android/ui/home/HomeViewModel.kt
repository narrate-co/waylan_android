package com.words.android.ui.home

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.words.android.data.repository.Word
import com.words.android.data.repository.WordRepository
import com.words.android.di.UserScope
import com.words.android.ui.list.ListFragment
import javax.inject.Inject

@UserScope
class HomeViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    fun getListPreview(type: ListFragment.ListType): LiveData<String> {
        return Transformations.map(when (type) {
            ListFragment.ListType.TRENDING -> wordRepository.getFavorites(4L)
            ListFragment.ListType.RECENT -> wordRepository.getRecents(4L)
            ListFragment.ListType.FAVORITE -> wordRepository.getFavorites(4L)
        }) { list ->
            val previewWords = list.mapNotNull { it.userWord?.word }
            if (previewWords.isNotEmpty()) previewWords.reduce { acc, word -> "$acc, $word" } else "---"
        }
    }
}