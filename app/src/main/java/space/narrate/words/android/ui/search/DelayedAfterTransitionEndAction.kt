package space.narrate.words.android.ui.search

import androidx.lifecycle.LifecycleOwner
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import space.narrate.words.android.util.widget.DelayedLifecycleAction


/**
 * A class that invokes a function after [transition] has ended <i>and</i> after [delay] has elapsed
 */
class DelayedAfterTransitionEndAction(
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