package com.wordsdict.android.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.prefs.UserPreferenceStore
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.di.UserScope
import com.wordsdict.android.ui.search.Period
import javax.inject.Inject

/**
 * ViewModel for [ListFragment]
 */
class ListViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userPreferenceStore: UserPreferenceStore
): ViewModel() {

    /**
     * @return whether or not the user has previously seen and dismissed an onboarding
     *  banner for [type]
     */
    fun getHasSeenBanner(type: ListFragment.ListType): Boolean =
            when (type) {
                ListFragment.ListType.TRENDING -> userPreferenceStore.hasSeenTrendingBanner
                ListFragment.ListType.RECENT -> userPreferenceStore.hasSeenRecentsBanner
                ListFragment.ListType.FAVORITE -> userPreferenceStore.hasSeenFavoritesBanner
            }

    /**
     * Set the underlying preference dictating whether or not the user has seen and dismissed
     * the onboarding banner for [type]
     */
    fun setHasSeenBanner(type: ListFragment.ListType, value: Boolean) {
        when (type) {
            ListFragment.ListType.TRENDING -> userPreferenceStore.hasSeenTrendingBanner = value
            ListFragment.ListType.RECENT -> userPreferenceStore.hasSeenRecentsBanner = value
            ListFragment.ListType.FAVORITE -> userPreferenceStore.hasSeenFavoritesBanner = value
        }
    }

    fun getListFilter(type: ListFragment.ListType): LiveData<List<Period>> {
        return when (type) {
            ListFragment.ListType.TRENDING -> userPreferenceStore.trendingListFilterLive
            ListFragment.ListType.RECENT -> userPreferenceStore.recentsListFilterLive
            ListFragment.ListType.FAVORITE -> userPreferenceStore.favoritesListFilterLive
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

