package com.wordsdict.android.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.wordsdict.android.data.prefs.UserPreferenceStore
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.di.UserScope
import javax.inject.Inject

@UserScope
class ListViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userPreferenceStore: UserPreferenceStore
): ViewModel() {

    fun getHasSeenBanner(type: ListFragment.ListType): Boolean =
            when (type) {
                ListFragment.ListType.TRENDING -> userPreferenceStore.hasSeenTrendingBanner
                ListFragment.ListType.RECENT -> userPreferenceStore.hasSeenRecentsBanner
                ListFragment.ListType.FAVORITE -> userPreferenceStore.hasSeenFavoritesBanner
            }

    fun getHasSeenBannerLive(type: ListFragment.ListType): LiveData<Boolean> =
            when (type) {
                ListFragment.ListType.TRENDING -> userPreferenceStore.hasSeenTrendingBannerLive
                ListFragment.ListType.RECENT -> userPreferenceStore.hasSeenRecentsBannerLive
                ListFragment.ListType.FAVORITE -> userPreferenceStore.hasSeenFavoritesBannerLive
            }

    fun setHasSeenBanner(type: ListFragment.ListType, value: Boolean) {
        when (type) {
            ListFragment.ListType.TRENDING -> userPreferenceStore.hasSeenTrendingBanner = value
            ListFragment.ListType.RECENT -> userPreferenceStore.hasSeenRecentsBanner = value
            ListFragment.ListType.FAVORITE -> userPreferenceStore.hasSeenFavoritesBanner = value
        }
    }

    fun getList(type: ListFragment.ListType): LiveData<List<WordSource>> {
        return when (type) {
            ListFragment.ListType.TRENDING -> wordRepository.getTrending(25L)
            ListFragment.ListType.RECENT -> wordRepository.getRecents(25L)
            ListFragment.ListType.FAVORITE -> wordRepository.getFavorites(25L)
        }
    }
}

