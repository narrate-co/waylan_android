package com.wordsdict.android.util

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.OrientationEventListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.PreferenceRepository

class OrientationManager(
        private val context: Context
): OrientationEventListener(context) {

    private var orientation: MutableLiveData<Info> = MutableLiveData()

    data class Info(
            val currentOrientation: Int,
            val prevOrientation: Int,
            val nextOrientation: Int
    ) {

        val overallPrevOrientation: Int
            get() {
                return if (prevOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || prevOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    Configuration.ORIENTATION_LANDSCAPE
                } else {
                    Configuration.ORIENTATION_PORTRAIT
                }
            }


        val overallNextOrientation: Int
            get() {
                return if (nextOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || nextOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    Configuration.ORIENTATION_LANDSCAPE
                } else {
                    Configuration.ORIENTATION_PORTRAIT
                }
            }
    }

    /**
     * A live data object that will only be called when there is a change in orientation
     */
    fun getOrientation(): LiveData<Info> = orientation

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

        if (orientation.value?.nextOrientation != or) {
            val info = Info(context.resources.configuration.orientation, orientation.value?.nextOrientation ?: ORIENTATION_UNKNOWN, or)
            println("OrientationManager::setting new orientation value = $info")
            orientation.value = info
        }
    }

}