package space.narrate.waylan.android.util

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * A class which holds [OnSlideAction]s and [OnStateChangedAction]s and calls
 * each held function upon their respective BottomSheetCallback being called. This is a helper
 * for when different functions should be added/removed to a BottomSheetCallback without the
 * need for numberous BottomSheetCallbacks needing to be created and added to a BottomSheetBehavior.
 *
 * All added [OnSlideAction]s will be run when [BottomSheetBehavior.BottomSheetCallback.onSlide]
 * is called
 *
 * All added [OnStateChangedAction]s will be run when
 * [BottomSheetBehavior.BottomSheetCallback.onStateChanged] is called
 */
class BottomSheetCallbackCollection: BottomSheetBehavior.BottomSheetCallback() {

    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()

    var currentSlide: Float = 0.0F
        private set(value) {
            field = value
            _currentSlideLive.value = value
        }

    private val _currentSlideLive: MutableLiveData<Float> = MutableLiveData()
    val currentSlideLive: LiveData<Float>
        get() = _currentSlideLive

    var currentState: Int = BottomSheetBehavior.STATE_HIDDEN
        private set(value) {
            field = value
            _currentStateLive.value = value
        }

    private val _currentStateLive: MutableLiveData<Int> = MutableLiveData()
    val currentStateLive: LiveData<Int>
        get() = _currentStateLive

    fun addOnSlideAction(action: (sheet: View, slideOffset: Float) -> Unit) {
        addOnSlideAction(object : OnSlideAction {
            override fun onSlide(sheet: View, slideOffset: Float) {
                action(sheet, slideOffset)
            }
        })
    }

    /**
     * Add an [OnSlideAction] to be run when this [BottomSheetBehavior.BottomSheetCallback]'s
     * [BottomSheetBehavior.BottomSheetCallback.onSlide] is called.
     */
    fun addOnSlideAction(action: OnSlideAction) {
        onSlideActions.add(action)
    }

    fun addOnStateChangedAction(action: (sheet: View, newState: Int) -> Unit) {
        addOnStateChangedAction(object : OnStateChangedAction {
            override fun onStateChanged(sheet: View, newState: Int) {
                action(sheet, newState)
            }
        })
    }

    /**
     * Add an [OnStateChangedAction] to be run when this [BottomSheetBehavior.BottomSheetCallback]'s
     * [BottomSheetBehavior.BottomSheetCallback.onStateChanged] is called.
     */
    fun addOnStateChangedAction(action: OnStateChangedAction) {
        onStateChangedActions.add(action)
    }


    override fun onSlide(view: View, offset: Float) {
        currentSlide = offset
        onSlideActions.forEach { it.onSlide(view, offset) }
    }

    override fun onStateChanged(view: View, newState: Int) {
        currentState = newState
        onStateChangedActions.forEach { it.onStateChanged(view, newState) }
    }

}