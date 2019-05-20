package space.narrate.words.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

/**
 * Helper extension to chain Transform.map to the end of a LiveData object.
 */
fun <X, Y> LiveData<X>.mapTransform(block: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, block)
}

/**
 * Helper extension to chain Transform.switchMap to the end of a LiveData object.
 */
fun <X, Y> LiveData<X>.switchMapTransform(block: (X) -> LiveData<Y>): LiveData<Y> {
    return Transformations.switchMap(this, block)
}

/**
 * Helper extension to add a Transform.map to the end of a LiveData object which only gets
 * called when the [on] LiveData value changes.
 */
fun <X, Y> LiveData<X>.mapOnTransform(on: LiveData<Y>, block: (X, Y) -> X): LiveData<X> {
    return Transformations.switchMap(on) { y ->
        Transformations.map(this) { x ->
            block(x, y)
        }
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
