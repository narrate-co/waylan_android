package com.wordsdict.android.ui.search

import androidx.lifecycle.LifecycleOwner
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter


/**
 * A class that invokes a function after [transition] has ended and after [delay] has passed
 */
class TransitionEndAction(
        owner: LifecycleOwner,
        private val transition: Transition,
        delay: Long, onEndAction: () -> Unit
): DelayedLifecycleAction(owner, delay, onEndAction) {

    private val transitionListener = object : TransitionListenerAdapter() {
        override fun onTransitionCancel(transition: Transition) {
            cancel()
        }
        override fun onTransitionEnd(transition: Transition) {
            run()
        }
    }

    init {
        transition.addListener(transitionListener)
    }

    override fun cancel() {
        super.cancel()
        transition.removeListener(transitionListener)
    }

}