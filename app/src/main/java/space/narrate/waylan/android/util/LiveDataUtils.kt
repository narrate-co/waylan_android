package space.narrate.waylan.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveDataUtils {
    fun <T> empty(): LiveData<T> = MutableLiveData<T>()
}