package space.narrate.waylan.core.util

import androidx.lifecycle.MutableLiveData

class DefaultingMutableLiveData<T>(private val default: T) : MutableLiveData<T>() {

    val valueOrDefault: T
        get() = value ?: default

    init {
        setDefault()
    }

    /**
     * Resets the value of this LiveData object to its default value
     */
    fun setDefault() {
        value = default
    }

}