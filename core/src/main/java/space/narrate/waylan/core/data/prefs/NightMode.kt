package space.narrate.waylan.core.data.prefs

import androidx.appcompat.app.AppCompatDelegate
import space.narrate.waylan.core.R

enum class NightMode(val value: Int, val titleRes: Int, val descRes: Int) {
    YES(
        AppCompatDelegate.MODE_NIGHT_YES,
        R.string.settings_night_mode_yes_title,
        R.string.settings_night_mode_yes_desc
    ),
    NO(
        AppCompatDelegate.MODE_NIGHT_NO,
        R.string.settings_night_mode_no_title,
        R.string.settings_night_mode_no_desc
    ),
    BATTERY_SAVER(
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
        R.string.settings_night_mode_set_by_battery_saver_title,
        R.string.settings_night_mode_set_by_battery_saver_desc
    ),
    SYSTEM_DEFAULT(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        R.string.settings_night_mode_system_default_title,
        R.string.settings_night_mode_system_default_desc
    );

    companion object {
        fun fromAppCompatDelegate(value: Int): NightMode {
            return when (value) {
                AppCompatDelegate.MODE_NIGHT_YES -> YES
                AppCompatDelegate.MODE_NIGHT_NO -> NO
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> BATTERY_SAVER
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> SYSTEM_DEFAULT
                else -> SYSTEM_DEFAULT
            }
        }
    }
}