package space.narrate.words.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveDataHelper {
    fun <T> empty(): LiveData<T> = MutableLiveData<T>()
}
