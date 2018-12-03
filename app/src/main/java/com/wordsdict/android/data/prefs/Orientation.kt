package com.wordsdict.android.data.prefs

import android.content.pm.ActivityInfo
import com.wordsdict.android.R


/**
 * All possible orientations Words supports
 *
 * A user can explicitly lock the app to any of the below [Orientation]s
 */
enum class Orientation(val value: Int, val title: Int, val desc: Int) {
    UNSPECIFIED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, R.string.settings_orientation_unspecified_title, R.string.settings_orientation_unspecified_desc),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, R.string.settings_orientation_portrait_title, R.string.settings_orientation_portrait_desc),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, R.string.settings_orientation_landscape_title, R.string.settings_orientation_landscape_desc),
    LANDSCAPE_REVERSE(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, R.string.settings_orientation_landscape_reverse_title, R.string.settings_orientation_landscape_reverse_desc);

    companion object {
        fun fromActivityInfoScreenOrientation(value: Int): Orientation {
            return when (value) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> Orientation.UNSPECIFIED
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> Orientation.LANDSCAPE_REVERSE
                else -> Orientation.UNSPECIFIED
            }
        }
    }
}