package space.narrate.waylan.core.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveDataUtils {
    fun <T> empty(): LiveData<T> = MutableLiveData<T>()
}