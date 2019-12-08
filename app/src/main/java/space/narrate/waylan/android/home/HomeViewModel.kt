package space.narrate.waylan.android.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.repo.WordRepository
import space.narrate.waylan.android.ui.list.ListType
import space.narrate.waylan.core.util.mapTransform

/**
 * ViewModel for [HomeFragment]
 */
class HomeViewModel(private val wordRepository: WordRepository) : ViewModel() {

    val list: LiveData<List<HomeItemModel>>
        get() = HomeItemListMediatorLiveData().apply {
            addSource(
                wordRepository.getGlobalWordTrending(PREVIEW_LIMIT)
                    .mapTransform { trends ->
                        HomeItemModel.ItemModel(
                            ListType.TRENDING,
                            R.string.title_trending,
                            trends.map { it.word }.toPreview
                        )
                    }
            )

            addSource(
                wordRepository.getUserWordRecents(PREVIEW_LIMIT)
                    .mapTransform { recs ->
                        HomeItemModel.ItemModel(
                            ListType.RECENT,
                            R.string.title_recent,
                            recs.map { it.word }.toPreview
                        )
                    }
            )

            addSource(
                wordRepository.getUserWordFavorites(PREVIEW_LIMIT)
                    .mapTransform { favs ->
                        HomeItemModel.ItemModel(
                            ListType.FAVORITE,
                            R.string.title_favorite,
                            favs.map { it.word }.toPreview
                        )
                    }
            )
        }

    private val List<String>.toPreview: String
        get() = if (isNotEmpty()) reduce { acc, s -> "$acc, $s" } else ""

    companion object {
        private const val PREVIEW_LIMIT = 4L
    }
}