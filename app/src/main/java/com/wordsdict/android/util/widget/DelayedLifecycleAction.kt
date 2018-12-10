package com.wordsdict.android.util.widget

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A class that invokes a function after a delay only if the LifecycleOwner has not passed through
 * [Lifecycle.Event.ON_STOP]
 *
 * Useful for delayed UI functions such as animations.
 *
 * TODO extend to support an action to be run when canceled
 */
open class DelayedLifecycleAction(
        owner: LifecycleOwner,
        private val delay: Long,
        private val action: () -> Unit
): LifecycleObserver {

    private var job: Job? = null
    private var canceled: Boolean = false

    init {
        owner.lifecycle.addObserver(this)
    }

    fun run(): DelayedLifecycleAction {
        if (!canceled) {
            job = launch(UI) {
                delay(delay)
                if (!canceled) {
                    action()
                }
            }
        }

        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        cancel()
    }

    open fun cancel() {
        canceled = true
        job?.cancel()
    }
}

