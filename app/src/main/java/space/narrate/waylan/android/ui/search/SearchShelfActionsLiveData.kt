package space.narrate.waylan.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import space.narrate.waylan.android.util.SoftInputModel
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.util.MergedListLiveData
import space.narrate.waylan.core.util.mapOnTransform
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.switchMapTransform

/**
 * A LiveData object that handles the logic of watching the values relevant for
 * showing search shelf actions and emits values when (and only when) the UI should be changed.
 */
class SearchShelfActionsLiveData(
    private val currentDestination: LiveData<Destination>,
    private val currentUserWord: LiveData<UserWord>,
    private val trendingListFilter: LiveData<List<Period>>,
    private val sheetOffset: LiveData<Float>,
    private val sheetState: LiveData<Int>,
    private val softInputState: LiveData<SoftInputModel>
) : MediatorLiveData<SearchShelfActionsModel>() {

    data class SheetKeyboardState(
        val sheetOffset: Float,
        val sheetState: Int,
        val softInputState: SoftInputModel
    )

    class SheetKeyboardStateLiveData(
        sheetOffset: LiveData<Float>,
        sheetState: LiveData<Int>,
        softInputState: LiveData<SoftInputModel>
    ) : MediatorLiveData<SheetKeyboardState>() {

        private var lastOffset: Float = 0F
        private var lastSheetState: Int = BottomSheetBehavior.STATE_HIDDEN
        private var lastSoftInputState: SoftInputModel? = null

        init {
            addSource(sheetOffset) { onSheetOffsetChanged(it) }
            addSource(sheetState) { onSheetStateChanged(it) }
            addSource(softInputState) { onSoftInputStateChanged(it) }
        }

        private fun onSheetOffsetChanged(newOffset: Float) {
            lastOffset = newOffset
            // never update this live data's value from an offset change since the offset
            // isn't something we care about downstream and it causes a lot of re-emissions
        }

        private fun onSheetStateChanged(newState: Int) {
            if (lastSheetState == null || lastSheetState != newState) {
                lastSheetState = newState
                maybePostValue()
            }
        }

        private fun onSoftInputStateChanged(newState: SoftInputModel) {
            if (lastSoftInputState == null || lastSoftInputState?.isOpen != newState.isOpen) {
                lastSoftInputState = newState
                maybePostValue()
            }
        }

        private fun maybePostValue() {
            val offset = lastOffset
            val sheetState = lastSheetState
            val softInputState = lastSoftInputState
            if (offset != null && sheetState != null && softInputState != null) {
                postValue(SheetKeyboardState(offset, sheetState, softInputState))
            }
        }
    }

    private val _sheetKeyboardState: LiveData<SheetKeyboardState>
        get() = SheetKeyboardStateLiveData(sheetOffset, sheetState, softInputState)

    private var lastSearchShelfModelValue: SearchShelfActionsModel? = null

    private val searchShelfModel: LiveData<SearchShelfActionsModel>
        get() = currentDestination
            .switchMapTransform { dest ->
                val model: LiveData<SearchShelfActionsModel> = when (dest) {
                    Destination.DETAILS ->
                        currentUserWord.mapTransform {
                            SearchShelfActionsModel.DetailsShelfActions(it)
                        }
                    Destination.TRENDING ->
                        trendingListFilter.mapTransform {
                            SearchShelfActionsModel.ListShelfActions(it.isNotEmpty())
                        }
                    else -> {
                        val data = MutableLiveData<SearchShelfActionsModel>()
                        data.value = SearchShelfActionsModel.None()
                        data
                    }
                }
                model
            }
            .mapOnTransform(_sheetKeyboardState) { model, sheetKeyboardState ->
                val isSheetExpanded = sheetKeyboardState.sheetState != BottomSheetBehavior.STATE_HIDDEN &&
                    sheetKeyboardState.sheetState != BottomSheetBehavior.STATE_COLLAPSED
                val isKeyboardOpen = sheetKeyboardState.softInputState.isOpen
                if (isSheetExpanded || isKeyboardOpen) {
                    SearchShelfActionsModel.SheetKeyboardControllerActions(isSheetExpanded, isKeyboardOpen)
                } else {
                    model
                }
            }

    init {
        searchShelfModel.observeForever {
            if (lastSearchShelfModelValue == null
                || lastSearchShelfModelValue?.isContentSameAs(it) == false)  {
                println("SearchShelfActions - $it")
                lastSearchShelfModelValue = it
                postValue(it)
            }
        }
    }
}
