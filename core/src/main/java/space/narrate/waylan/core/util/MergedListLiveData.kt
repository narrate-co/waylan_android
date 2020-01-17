package space.narrate.waylan.core.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

/**
 * A LiveData class that waits for two sources to be fulfilled before setting [LiveData.setValue].
 */
class MergedListLiveData<T>(
    sources: List<LiveData<out Any>>,
    private val merge: (results: List<Any>) -> T
): MediatorLiveData<T>() {

    val list = MutableList<Any?>(sources.size) { null }

    init {
        sources.forEachIndexed { index, any ->
            super.addSource(any) {
                list[index] = it
                maybePostValue()
            }
        }
    }

    private fun maybePostValue() {
        // Only post once all sources have emitted data
        val copy = list.toList()
        if (copy.any { it == null }) return

        @Suppress("UNCHECKED_CAST")
        postValue(merge(copy as List<Any>))
    }

    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException("MergedLiveData cannot add sources")
    }

    override fun <S : Any?> removeSource(toRemote: LiveData<S>) {
        throw UnsupportedOperationException("MergedLiveData cannot remove sources")
    }
}

