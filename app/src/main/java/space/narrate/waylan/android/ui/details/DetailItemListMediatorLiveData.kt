package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

/**
 * A LiveData that manages all independent LiveData values from each source and handles updating
 * any observers when any of the sources change.
 *
 * This LiveData object delays when it is first created to give sources a chance to load before
 * posting any values. This avoids posting a new list for every source that is loaded initially.
 * Since most sources load very quickly (< 250 milliseconds), collecting these sources and then
 * "batch" posting the initial list after [INITIAL_POST_DELAY_MILLIS] will cause any adapters listening
 * to this source to load items more smoothly/in unison without increasing perceived latency.
 */
class DetailItemListMediatorLiveData : MediatorLiveData<List<DetailItemModel>>() {

    private var hasPassedInitialPostValueDelay = false
    private var awaitingPostValue = false

    private var mwModel: DetailItemModel? = null
    private var mwThesaurusModel: DetailItemModel? = null
    private var wordsetModel: DetailItemModel? = null
    private var examplesModel: DetailItemModel? = null

    init {
        // Force clear any observers if they are displaying an old instance's list.
        postValue(emptyList())

        // Give a new list a small delay before dispatching an initial value to maybe allow more
        // than one source to be retrieved before calling postValue, minimising the number of times
        // postValue is called while balancing first-load latency perception.
        CoroutineScope(Dispatchers.Main).launch {
            delay(INITIAL_POST_DELAY_MILLIS)
            hasPassedInitialPostValueDelay = true
            if (awaitingPostValue) {
                dispatchList()
            }
        }
    }

    fun <T : DetailItemModel> addSource(data: LiveData<T>) {
        addSource(data) { set(it) }
    }

    private fun <T : DetailItemModel> set(item: T) {
        when (item.itemType) {
            DetailItemType.MERRIAM_WEBSTER -> {
                if (shouldUpdateList(mwModel, item)) {
                    mwModel = item
                    maybeDispatchList()
                }
            }
            DetailItemType.MERRIAM_WEBSTER_THESAURUS -> {
                if (shouldUpdateList(mwThesaurusModel, item)) {
                    mwThesaurusModel = item
                    maybeDispatchList()
                }
            }
            DetailItemType.WORDSET -> {
                if (shouldUpdateList(wordsetModel, item)) {
                    wordsetModel = item
                    maybeDispatchList()
                }
            }
            DetailItemType.EXAMPLE -> {
                if (shouldUpdateList(examplesModel, item)) {
                    examplesModel = item
                    maybeDispatchList()
                }
            }
        }
    }

    private fun shouldUpdateList(oldItem: DetailItemModel?, newItem: DetailItemModel): Boolean {
        if (oldItem == null) return true
        return !oldItem.isSameAs(newItem) || !oldItem.isContentSameAs(newItem)
    }

    private fun maybeDispatchList() {
        // Go ahead and dispatch this list if we've passed our initial collection/batching delay,
        // otherwise, mark this object as awaiting to be posted to observers.
        if (hasPassedInitialPostValueDelay) {
            dispatchList()
        } else {
            awaitingPostValue = true
        }
    }

    private fun dispatchList() {
        val newList = listOfNotNull(
            mwModel,
            mwThesaurusModel,
            wordsetModel,
            examplesModel
        ).sortedBy { it.itemType.order }

        postValue(newList)
        awaitingPostValue = false
    }

    companion object {
        private const val INITIAL_POST_DELAY_MILLIS = 250L
    }
}