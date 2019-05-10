package space.narrate.words.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

fun <X, Y> LiveData<X>.mapTransform(block: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, block)
}

fun <X, Y> LiveData<X>.switchMapTransform(block: (X) -> LiveData<Y>): LiveData<Y> {
    return Transformations.switchMap(this, block)
}

fun <X, Y> LiveData<X>.mapOnTransform(on: LiveData<Y>, block: (X, Y) -> X): LiveData<X> {
    return Transformations.switchMap(on) { y ->
        Transformations.map(this) { x ->
            block(x, y)
        }
    }
}