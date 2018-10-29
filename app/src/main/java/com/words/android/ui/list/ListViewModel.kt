package com.words.android.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.data.repository.WordRepository
import com.words.android.data.repository.WordSource
import com.words.android.di.UserScope
import javax.inject.Inject

@UserScope
class ListViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val userPreferenceRepository: UserPreferenceRepository
): ViewModel() {

    fun getHasSeenBanner(type: ListFragment.ListType): LiveData<Boolean> =
            when (type) {
                ListFragment.ListType.TRENDING -> userPreferenceRepository.hasSeenTrendingBannerLive
                ListFragment.ListType.RECENT -> userPreferenceRepository.hasSeenRecentsBannerLive
                ListFragment.ListType.FAVORITE -> userPreferenceRepository.hasSeenFavoritesBannerLive
            }

    fun setHasSeenBanner(type: ListFragment.ListType, value: Boolean) {
        when (type) {
            ListFragment.ListType.TRENDING -> userPreferenceRepository.hasSeenTrendingBanner = value
            ListFragment.ListType.RECENT -> userPreferenceRepository.hasSeenRecentsBanner = value
            ListFragment.ListType.FAVORITE -> userPreferenceRepository.hasSeenFavoritesBanner = value
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

