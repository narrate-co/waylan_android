package space.narrate.words.android.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.data.repository.WordSource
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.ui.list.ListFragment
import javax.inject.Inject

/**
 * ViewModel for [ListFragment]
 */
class ListViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userRepository: UserRepository
): ViewModel() {

    /**
     * @return whether or not the user has previously seen and dismissed an onboarding
     *  banner for [type]
     */
    fun getHasSeenBanner(type: ListFragment.ListType): Boolean =
            when (type) {
                ListFragment.ListType.TRENDING -> userRepository.hasSeenTrendingBanner
                ListFragment.ListType.RECENT -> userRepository.hasSeenRecentsBanner
                ListFragment.ListType.FAVORITE -> userRepository.hasSeenFavoritesBanner
            }

    /**
     * Set the underlying preference dictating whether or not the user has seen and dismissed
     * the onboarding banner for [type]
     */
    fun setHasSeenBanner(type: ListFragment.ListType, value: Boolean) {
        when (type) {
            ListFragment.ListType.TRENDING -> userRepository.hasSeenTrendingBanner = value
            ListFragment.ListType.RECENT -> userRepository.hasSeenRecentsBanner = value
            ListFragment.ListType.FAVORITE -> userRepository.hasSeenFavoritesBanner = value
        }
    }

    fun getListFilter(type: ListFragment.ListType): LiveData<List<Period>> {
        return when (type) {
            ListFragment.ListType.TRENDING -> userRepository.trendingListFilterLive
            ListFragment.ListType.RECENT -> userRepository.recentsListFilterLive
            ListFragment.ListType.FAVORITE -> userRepository.favoritesListFilterLive
        }
    }

    /**
     * Get a list of either [FirestoreUserSource] or [FirestoreGlobalSource] items which
     * correspond to the given [type]
     */
    fun getList(type: ListFragment.ListType, filter: List<Period>): LiveData<List<WordSource>> {
        return when (type) {
            ListFragment.ListType.TRENDING -> wordRepository.getTrending(25L, filter)
            ListFragment.ListType.RECENT -> wordRepository.getRecents(25L)
            ListFragment.ListType.FAVORITE -> wordRepository.getFavorites(25L)
        } as LiveData<List<WordSource>>
    }
}

