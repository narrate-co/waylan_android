package com.wordsdict.android.ui.search

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

typealias OnSlideAction = (View, Float) -> Unit
typealias OnStateChangedAction = (View, Int) -> Unit

class SearchSheetCallback: BottomSheetBehavior.BottomSheetCallback() {


    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()

    fun addOnStateChangedAction(action: OnStateChangedAction) {
        onStateChangedActions.add(action)
    }

    fun addOnSlideAction(action: OnSlideAction) {
        onSlideActions.add(action)
    }

    override fun onSlide(view: View, offset: Float) {
        onSlideActions.forEach { it(view, offset) }
    }

    override fun onStateChanged(view: View, newState: Int) {
        onStateChangedActions.forEach { it(view, newState) }
    }

}