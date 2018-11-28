package com.wordsdict.android.ui.search

import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TransitionEndAction(private val transition: Transition, showLength: Long, onEndAction: () -> Unit) {

    private var canceled: Boolean = false

    private val transitionListener = object : TransitionListenerAdapter() {
        override fun onTransitionCancel(transition: Transition) {
            canceled = true
        }
        override fun onTransitionEnd(transition: Transition) {
            if (!canceled) {
                launch(UI) {
                    delay(showLength)
                    if (!canceled) {
                        onEndAction()
                    }
                }
            }
        }
    }

    init {
        transition.addListener(transitionListener)
    }

    fun cancel() {
        canceled = true
        transition.removeListener(transitionListener)
    }
}