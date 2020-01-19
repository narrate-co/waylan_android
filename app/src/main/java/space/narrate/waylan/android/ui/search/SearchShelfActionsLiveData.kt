package space.narrate.waylan.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import space.narrate.waylan.android.util.SoftInputModel
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.util.mapOnTransform
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.switchMapTransform
import space.narrate.waylan.core.util.toLiveData

/**
 * A LiveData object that handles the logic of watching the values relevant for
 * showing search shelf actions and emits values when (and only when) the UI should be changed.
 */
class SearchShelfActionsLiveData(
    private val currentDestination: LiveData<Destination>,
    private val currentUserWord: LiveData<UserWord>,
    private val trendingListFilter: LiveData<List<Period>>,
    private val searchSheetOffset: LiveData<Float>,
    private val searchSheetState: LiveData<Int>,
    private val contextualSheetOffset: LiveData<Float>,
    private val contextualSheetState: LiveData<Int>,
    private val softInputState: LiveData<SoftInputModel>
) : MediatorLiveData<SearchShelfActionsRowModel>() {

    data class SheetKeyboardState(
        val searchSheetOffset: Float,
        val searchSheetState: Int,
        val contextualSheetOffset: Float,
        val contextualSheetState: Int,
        val softInputState: SoftInputModel
    )

    class SheetKeyboardStateLiveData(
        searchSheetOffset: LiveData<Float>,
        searchSheetState: LiveData<Int>,
        contextualSheetOffset: LiveData<Float>,
        contextualSheetState: LiveData<Int>,
        softInputState: LiveData<SoftInputModel>
    ) : MediatorLiveData<SheetKeyboardState>() {

        private var lastSearchSheetOffset: Float = 0F
        private var lastSearchSheetState: Int = BottomSheetBehavior.STATE_HIDDEN
        private var lastContextualSheetOffset: Float = 0F
        private var lastContextualSheetState: Int = BottomSheetBehavior.STATE_HIDDEN

        // As the only nullable value, SoftInputModel is the only value this live data will wait
        // for before posting any values.
        private var lastSoftInputState: SoftInputModel? = null

        init {
            addSource(searchSheetOffset) { onSearchSheetOffsetChanged(it) }
            addSource(searchSheetState) { onSearchSheetStateChanged(it) }
            addSource(contextualSheetOffset) { onContextualSheetOffsetChanged(it) }
            addSource(contextualSheetState) { onContextualSheetStateChanged(it) }
            addSource(softInputState) { onSoftInputStateChanged(it) }
        }

        private fun onSearchSheetOffsetChanged(newOffset: Float) {
            lastSearchSheetOffset = newOffset
            // never update this live data's value from an offset change since the offset
            // isn't something we care about downstream and it causes a lot of re-emissions
        }

        private fun onSearchSheetStateChanged(newState: Int) {
            if (lastSearchSheetState != newState) {
                lastSearchSheetState = newState
                maybePostValue()
            }
        }

        private fun onContextualSheetOffsetChanged(newOffset: Float) {
            lastContextualSheetOffset = newOffset
            // do nothing
        }

        private fun onContextualSheetStateChanged(newState: Int) {
            if (lastContextualSheetState != newState) {
                lastContextualSheetState = newState
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
            val searchOffset = lastSearchSheetOffset
            val searchSheetState = lastSearchSheetState
            val contextualOffset = lastContextualSheetOffset
            val contextualSheetState = lastContextualSheetState
            val softInputState = lastSoftInputState
            if (softInputState != null) {
                postValue(
                    SheetKeyboardState(
                        searchOffset,
                        searchSheetState,
                        contextualOffset,
                        contextualSheetState,
                        softInputState
                    )
                )
            }
        }
    }

    private val _sheetKeyboardState: LiveData<SheetKeyboardState>
        get() = SheetKeyboardStateLiveData(
            searchSheetOffset,
            searchSheetState,
            contextualSheetOffset,
            contextualSheetState,
            softInputState
        )

    private var lastSearchShelfModelValue: SearchShelfActionsRowModel? = null

    private val searchShelfRowModel: LiveData<SearchShelfActionsRowModel>
        get() = currentDestination
            .switchMapTransform { dest ->
                val model: LiveData<SearchShelfActionsRowModel> = when (dest) {
                    Destination.DETAILS ->
                        currentUserWord.mapTransform {
                            SearchShelfActionsRowModel.DetailsShelfActions(it)
                        }
                    Destination.TRENDING ->
                        trendingListFilter.mapTransform {
                            SearchShelfActionsRowModel.ListShelfActions(it.isNotEmpty())
                        }
                    else -> SearchShelfActionsRowModel.None().toLiveData
                }
                model
            }
            .mapOnTransform(_sheetKeyboardState) { model, sheetKeyboardState ->
                val isSearchSheetExpanded =
                    sheetKeyboardState.searchSheetState != BottomSheetBehavior.STATE_HIDDEN &&
                    sheetKeyboardState.searchSheetState != BottomSheetBehavior.STATE_COLLAPSED
                val isContextualSheetExpanded =
                    sheetKeyboardState.contextualSheetState != BottomSheetBehavior.STATE_HIDDEN &&
                    sheetKeyboardState.contextualSheetState != BottomSheetBehavior.STATE_COLLAPSED
                val isKeyboardOpen = sheetKeyboardState.softInputState.isOpen

                if (isSearchSheetExpanded || isKeyboardOpen) {
                    SearchShelfActionsRowModel.SheetKeyboardControllerActions(
                        isSearchSheetExpanded,
                        isKeyboardOpen
                    )
                } else if (isContextualSheetExpanded) {
                    SearchShelfActionsRowModel.None()
                } else {
                    model
                }
            }

    init {
        searchShelfRowModel.observeForever {
            if (lastSearchShelfModelValue == null
                || lastSearchShelfModelValue?.isContentSameAs(it) == false)  {
                println("SearchShelfActions - $it")
                lastSearchShelfModelValue = it
                postValue(it)
            }
        }
    }
}
