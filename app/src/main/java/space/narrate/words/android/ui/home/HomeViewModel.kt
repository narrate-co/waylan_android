package space.narrate.words.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.ui.list.ListType
import space.narrate.words.android.util.mapTransform

/**
 * ViewModel for [HomeFragment]
 */
class HomeViewModel(private val wordRepository: WordRepository) : ViewModel() {

    fun getPreview(type: ListType): LiveData<String> {
        return (when (type) {
            ListType.TRENDING -> wordRepository.getGlobalWordTrending(PREVIEW_LIMIT)
                .mapTransform { trends ->
                    trends.map { it.word }
                }
            ListType.RECENT -> wordRepository.getUserWordRecents(PREVIEW_LIMIT)
                .mapTransform { recs ->
                    recs.map { it.word }
                }
            ListType.FAVORITE -> wordRepository.getUserWordFavorites(PREVIEW_LIMIT)
                .mapTransform { favs ->
                    favs.map { it.word }
                }
        })
            .mapTransform { list ->
                if (list.isNotEmpty()) {
                    list.reduce { acc, word -> "$acc, $word" }
                } else {
                    ""
                }
            }
    }

    companion object {
        private const val PREVIEW_LIMIT = 4L
    }
}