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

/**
 * ViewModel for [HomeFragment]
 */
@UserScope
class HomeViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    /**
     * Get a LiveData object which queries and observes a user's
     * [com.wordsdict.android.data.firestore.users.UserWord]'s depending on the [type] given. The
     * results are transformed into a simple comma separated string for easy display
     *
     * @return a comma separated string of the last 4 words (as they appear in the dictionary)
     *  from the users words of [type]
     */
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
            if (previewWords.isNotEmpty()) {
                previewWords.reduce { acc, word -> "$acc, $word" }
            } else {
                ""
            }
        }
    }
}