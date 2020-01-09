package space.narrate.waylan.settings.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import space.narrate.waylan.settings.R

/**
 * A UI Model to hold data to be displayed by the log in/sign out preference.
 */
class LogInSignOutModel(
    @StringRes val titleRes: Int = R.string.settings_log_in_sign_up_title,
    @StringRes private val descRes: Int = R.string.settings_log_in_sign_up_desc,
    private val descString: String = ""
) {
    fun getDesc(context: Context): String =
        if (descString.isNotBlank()) descString else context.getString(descRes)
}

