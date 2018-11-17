package com.wordsdict.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.repository.FirestoreGlobalSource
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.di.UserScope
import com.wordsdict.android.ui.list.ListFragment
import javax.inject.Inject

@UserScope
class HomeViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    fun getListPreview(type: ListFragment.ListType): LiveData<String> {
        return Transformations.map(when (type) {
            ListFragment.ListType.TRENDING -> wordRepository.getTrending(4L)
            ListFragment.ListType.RECENT -> wordRepository.getRecents(4L)
            ListFragment.ListType.FAVORITE -> wordRepository.getFavorites(4L)
        }) { list ->
            val previewWords = list.mapNotNull {
                when (it) {
                    is FirestoreUserSource -> it.userWord.word
                    is FirestoreGlobalSource -> it.globalWord.word
                    else -> ""
                }
            }
            if (previewWords.isNotEmpty()) previewWords.reduce { acc, word -> "$acc, $word" } else "---"
        }
    }
}