package com.wordsdict.android.util

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class OrientationManager(private val context: Context): OrientationEventListener(context) {

    private var orientation: MutableLiveData<Pair<Int, Int>> = MutableLiveData()

    /**
     * A live data object that will only be called when there is a change in orientation
     */
    fun getOrientation(): LiveData<Pair<Int, Int>> = orientation

    init {
        enable()
    }

    override fun onOrientationChanged(degrees: Int) {
        if (degrees == OrientationEventListener.ORIENTATION_UNKNOWN) return

        val or = when (degrees) {
            in 45..134 -> {
                // reverse landscape
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            in 135..224 -> {
                // reverse portrait
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
            in 225..314 -> {
                // landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            else -> {
                // portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        if (orientation.value == null || orientation.value?.second != or) {
            val pair = Pair(orientation.value?.second ?: ORIENTATION_UNKNOWN, or)
            orientation.value = pair
        }
    }

}