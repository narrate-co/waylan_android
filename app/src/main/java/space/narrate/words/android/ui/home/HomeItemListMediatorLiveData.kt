package space.narrate.words.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import space.narrate.words.android.R
import space.narrate.words.android.ui.list.ListType

class HomeItemListMediatorLiveData : MediatorLiveData<List<HomeItemModel>>() {

    private var trendingPreview = HomeItemModel.ItemModel(
        ListType.TRENDING,
        R.string.title_trending
    )
    private var recentPreview = HomeItemModel.ItemModel(
        ListType.RECENT,
        R.string.title_recent
    )
    private var favoritePreview = HomeItemModel.ItemModel(
        ListType.FAVORITE,
        R.string.title_favorite
    )

    private val divider = HomeItemModel.DividerModel

    private val settings = HomeItemModel.SettingsModel

    fun addSource(data: LiveData<HomeItemModel.ItemModel>) {
        addSource(data) {
            when (it.listType) {
                ListType.TRENDING -> {
                    if (shouldUpdateList(trendingPreview, it)) {
                        trendingPreview = it
                        updateList()
                    }
                }
                ListType.RECENT -> {
                    if (shouldUpdateList(recentPreview, it)) {
                        recentPreview = it
                        updateList()
                    }
                }
                ListType.FAVORITE -> {
                    if (shouldUpdateList(favoritePreview, it)) {
                        favoritePreview = it
                        updateList()
                    }
                }
            }
        }
    }

    private fun shouldUpdateList(
        oldItemModel: HomeItemModel.ItemModel,
        newItem: HomeItemModel.ItemModel
    ): Boolean {
        return !oldItemModel.isContentSameAs(newItem)
    }

    private fun updateList() {
        postValue(listOf(
            settings,
            divider,
            favoritePreview,
            recentPreview,
            trendingPreview
        ))
    }
}