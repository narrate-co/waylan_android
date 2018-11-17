package com.wordsdict.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveDataHelper {
    fun <T> empty(): LiveData<T> = MutableLiveData<T>()
}
