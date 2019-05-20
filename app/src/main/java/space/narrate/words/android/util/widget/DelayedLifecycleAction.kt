package space.narrate.words.android.util.widget

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
): LifecycleObserver, CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        owner.lifecycle.addObserver(this)
    }

    fun run(): DelayedLifecycleAction {
        if (!job.isCancelled) {
            launch {
                delay(delay)
                action()
            }
        }

        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        cancel()
    }

    open fun cancel() {
        job.cancel()
    }
}

