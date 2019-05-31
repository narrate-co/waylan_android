package space.narrate.words.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveDataTestUtils {

    fun <T> of(t: T): LiveData<T> = LiveDataTestUtils.of { t }

    fun <T> of(block: () -> T): LiveData<T> {
        val result = MutableLiveData<T>()
        result.value = block()
        return result
    }
}