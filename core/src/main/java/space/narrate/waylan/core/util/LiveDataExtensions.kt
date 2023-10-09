package space.narrate.waylan.core.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap

/**
 * Helper extension to add a Transform.map to the end of a LiveData object which only gets
 * called when the [on] LiveData value changes.
 */
fun <X, Y, Z> LiveData<X>.mapOnTransform(on: LiveData<Y>, block: (X, Y) -> Z): LiveData<Z> {
    return on.switchMap { y ->
        this.map { x ->
            block(x, y)
        }
    }
}

/**
 * Run any [block] of code when a value is posted to this LiveData object. This does not affect
 * the downstream value of this LiveData object.
 */
fun <X> LiveData<X>.doOnEmission(block: (X) -> Unit): LiveData<X> {
    return map {
        block(it)
        it
    }
}

/**
 * Helper extension to force a LiveData object to only emit non-null values.
 */
fun <X> LiveData<X?>.notNullTransform(): LiveData<X> {
    val result: MediatorLiveData<X> = MediatorLiveData()
    result.addSource(this) {
        if (it != null) result.value = it
    }
    return result
}

/**
 * Simple helper function to wrap any object in a LiveData.
 */
val <T> T.toLiveData: LiveData<T>
    get() {
        val liveData = MutableLiveData<T>()
        liveData.value = this
        return liveData
    }

/**
 * Helper function to only set a live data's value if the new value is different than the
 * current value.
 */
fun <T> MutableLiveData<T>.maybeSet(t: T) {
    if (value != t) {
        value = t
    }
}