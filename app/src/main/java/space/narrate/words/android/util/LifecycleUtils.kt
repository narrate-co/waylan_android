package space.narrate.words.android.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

fun Lifecycle.doOnResume(block: () -> Unit) {
    this.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            block()
        }
    })
}

fun Lifecycle.doOnNextResume(block: () -> Unit) {
    this.addObserver(object: LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            block()
            this@doOnNextResume.removeObserver(this)
        }
    })
}

fun Lifecycle.doOnDestroy(block: () -> Unit) {
    this.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            block()
        }
    })
}