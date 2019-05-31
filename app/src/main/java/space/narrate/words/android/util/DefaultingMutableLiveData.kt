package space.narrate.words.android.util

import androidx.lifecycle.MutableLiveData

class DefaultingMutableLiveData<T>(private val default: T) : MutableLiveData<T>() {

    val valueOrDefault: T
        get() = value ?: default
}