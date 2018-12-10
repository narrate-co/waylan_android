package com.wordsdict.android.ui.search

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

typealias OnSlideAction = (View, Float) -> Unit
typealias OnStateChangedAction = (View, Int) -> Unit

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
class SearchSheetCallback: BottomSheetBehavior.BottomSheetCallback() {

    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()

    /**
     * Add an [OnStateChangedAction] to be run when this [BottomSheetBehavior.BottomSheetCallback]'s
     * [BottomSheetBehavior.BottomSheetCallback.onStateChanged] is called.
     */
    fun addOnStateChangedAction(action: OnStateChangedAction) {
        onStateChangedActions.add(action)
    }

    /**
     * Remove an added [OnStateChangedAction]
     */
    fun removeOnStateChangedAction(action: OnStateChangedAction) {
        onStateChangedActions.remove(action)
    }

    /**
     * Add an [OnSlideAction] to be run when this [BottomSheetBehavior.BottomSheetCallback]'s
     * [BottomSheetBehavior.BottomSheetCallback.onSlide] is called.
     */
    fun addOnSlideAction(action: OnSlideAction) {
        onSlideActions.add(action)
    }

    /**
     * Remove an added [OnSlideAction]
     */
    fun removeOnSlideAction(action: OnSlideAction) {
        onSlideActions.remove(action)
    }

    override fun onSlide(view: View, offset: Float) {
        onSlideActions.forEach { it(view, offset) }
    }

    override fun onStateChanged(view: View, newState: Int) {
        onStateChangedActions.forEach { it(view, newState) }
    }

}