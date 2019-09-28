package space.narrate.waylan.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import java.lang.UnsupportedOperationException

/**
 * A LiveData class that waits for two sources to be fulfilled before setting [LiveData.setValue].
 */
class MergedLiveData<T, K, S>(
        source1: LiveData<T>,
        source2: LiveData<K>,
        private val merge: (data1: T, data2: K) -> S
): MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null

    init {
        super.addSource(source1) {
            data1 = it

            val d1 = data1
            val d2 = data2

            if (d1 != null && d2 != null) {
                value = merge(d1, d2)
            }
        }
        super.addSource(source2) {
            data2 = it

            val d1 = data1
            val d2 = data2

            if (d1 != null && d2 != null) {
                value = merge(d1, d2)
            }
        }
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException("MergedLiveData cannot add sources")
    }

    override fun <S : Any?> removeSource(toRemote: LiveData<S>) {
        throw UnsupportedOperationException("MergedLiveData cannot remove sources")
    }
}

