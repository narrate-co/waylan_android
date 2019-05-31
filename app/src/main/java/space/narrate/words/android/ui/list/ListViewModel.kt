package space.narrate.words.android.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.words.android.R
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.util.mapTransform
import space.narrate.words.android.util.switchMapTransform

/**
 * ViewModel for [ListFragment]
 */
class ListViewModel(
        private val wordRepository: WordRepository,
        private val userRepository: UserRepository
): ViewModel() {

    private val _listType: MutableLiveData<ListType> = MutableLiveData()
    val listType: LiveData<ListType>
        get() = _listType

    /**
     * Whether or not the user has seen the banner for this list type.
     */
    private val hasSeenHeader: LiveData<Boolean> = listType.switchMapTransform {
        when (it) {
            ListType.TRENDING -> userRepository.hasSeenTrendingBannerLive
            ListType.RECENT -> userRepository.hasSeenRecentsBannerLive
            ListType.FAVORITE -> userRepository.hasSeenFavoritesBannerLive
        }
    }

    val list: LiveData<List<ListItemModel>> = listType
        .switchMapTransform { type ->
            when (type) {
                ListType.TRENDING -> userRepository.trendingListFilterLive
                    .switchMapTransform { filter ->
                        wordRepository.getGlobalWordTrending(LIST_LIMIT, filter)
                    }
                    .mapTransform { globalWords ->
                        globalWords.map { ListItemModel.GlobalWordModel(it) }
                    }
                ListType.RECENT -> wordRepository.getUserWordRecents(LIST_LIMIT)
                    .mapTransform { userWords ->
                        userWords.map { ListItemModel.UserWordModel(it) }
                    }
                ListType.FAVORITE -> wordRepository.getUserWordFavorites(LIST_LIMIT)
                    .mapTransform { userWords ->
                        userWords.map { ListItemModel.UserWordModel(it) }
                    }
            } as LiveData<List<ListItemModel>>
        }
        .switchMapTransform { filteredItems ->
            hasSeenHeader.mapTransform { hasSeenHeader -> addHeader(filteredItems, hasSeenHeader) }
        }

    /**
     * Set the current list type to be observed.
     */
    fun setListType(type: ListType) {
        _listType.value = type
    }

    /**
     * Generate the header data model which should be shown for this list type.
     */
    private fun addHeader(list: List<ListItemModel>, hasSeenHeader: Boolean): List<ListItemModel> {
        val type = listType.value ?: ListType.TRENDING

        return if (list.isNullOrEmpty() || (!hasSeenHeader && type == ListType.TRENDING)) {
            // Add a header to the list
            val text = when (type) {
                ListType.TRENDING -> R.string.list_banner_trending_body
                ListType.RECENT -> R.string.list_banner_recents_body
                ListType.FAVORITE -> R.string.list_banner_favorites_body
            }
            val topButton = if (list.isEmpty()) R.string.list_banner_get_started_button else null
            val bottomButton = if (list.isEmpty()) null else R.string.list_banner_dismiss_button
            val header = ListItemModel.HeaderModel(text, topButton, bottomButton)

            list.toMutableList().apply {
                add(0, header)
            }
        } else {
            list
        }
    }

    fun onBannerDismissClicked() {
        when (listType.value) {
            ListType.TRENDING -> userRepository.hasSeenTrendingBanner = true
            ListType.RECENT -> userRepository.hasSeenRecentsBanner = true
            ListType.FAVORITE -> userRepository.hasSeenFavoritesBanner = true
        }
    }

    companion object {
        private const val LIST_LIMIT = 25L
    }
}

