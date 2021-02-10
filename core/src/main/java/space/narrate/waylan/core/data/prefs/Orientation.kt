package space.narrate.waylan.core.data.prefs

import android.content.pm.ActivityInfo
import space.narrate.waylan.core.R

/**
 * All possible screen orientations Words supports by allowing the user to explicitly set via
 * [UserRepository]
 */
enum class Orientation(val value: Int, val title: Int, val desc: Int) {
    UNSPECIFIED(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            R.string.settings_orientation_unspecified_title,
            R.string.settings_orientation_unspecified_desc
    ),
    PORTRAIT(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            R.string.settings_orientation_portrait_title,
            R.string.settings_orientation_portrait_desc
    ),
    LANDSCAPE(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            R.string.settings_orientation_landscape_title,
            R.string.settings_orientation_landscape_desc
    ),
    LANDSCAPE_REVERSE(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            R.string.settings_orientation_landscape_reverse_title,
            R.string.settings_orientation_landscape_reverse_desc
    );

    companion object {
        fun fromActivityInfoScreenOrientation(value: Int): Orientation {
            return when (value) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> UNSPECIFIED
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> LANDSCAPE_REVERSE
                else -> UNSPECIFIED
            }
        }
    }
}