package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class DetailItemListMediatorLiveData : MediatorLiveData<List<DetailItemModel>>() {

    private var titleModel: DetailItemModel.TitleModel? = null
    private var mwModel: DetailItemModel.MerriamWebsterModel? = null
    private var wordsetModel: DetailItemModel.WordsetModel? = null
    private var examplesModel: DetailItemModel.ExamplesModel? = null

    fun <T : DetailItemModel> addSource(data: LiveData<T>) {
        addSource(data) { set(it) }
    }

    private fun <T : DetailItemModel> set(item: T) {
        when (item) {
            is DetailItemModel.TitleModel -> {
                if (shouldUpdateList(titleModel, item)) {
                    titleModel = item
                    updateList()
                }
            }
            is DetailItemModel.MerriamWebsterModel -> {
                if (shouldUpdateList(mwModel, item)) {
                    mwModel = item
                    updateList()
                }
            }
            is DetailItemModel.WordsetModel -> {
                if (shouldUpdateList(wordsetModel, item)) {
                    wordsetModel = item
                    updateList()
                }
            }
            is DetailItemModel.ExamplesModel -> {
                if (shouldUpdateList(examplesModel, item)) {
                    examplesModel = item
                    updateList()
                }
            }
        }
    }

    private fun shouldUpdateList(oldItem: DetailItemModel?, newItem: DetailItemModel): Boolean {
        if (oldItem == null) return true

        return !oldItem.isSameAs(newItem) || !oldItem.isContentSameAs(newItem)
    }

    private fun updateList() {
        postValue(listOfNotNull(
            titleModel,
            mwModel,
            wordsetModel,
            examplesModel
        ))
    }

}