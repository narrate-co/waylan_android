package space.narrate.words.android.ui.dialog

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import space.narrate.words.android.R

/**
 * A [RoundedAlertDialog] that displays a list of radio button items, one for each night mode
 * preference option.
 */
class NightModeDialog : RoundedAlertDialog() {

    abstract class NightModeCallback {
        abstract fun onSelected(nightMode: Int)
        abstract fun onDismissed()
    }

    private var currentNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private var nightModeCallback: NightModeCallback? = null

    /**
     * Add all [AppCompatDelegate] night modes to the [RoundedAlertDialog]'s container to
     * be displayed
     */
    override fun setBuilderView(container: ViewGroup) {

        //Add radio buttons
        listOf(
            AppCompatDelegate.MODE_NIGHT_AUTO,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_NO
        ).forEach {
            val title = when (it) {
                AppCompatDelegate.MODE_NIGHT_AUTO ->
                    getString(R.string.settings_night_mode_auto_title)
                AppCompatDelegate.MODE_NIGHT_YES ->
                    getString(R.string.settings_night_mode_yes_title)
                AppCompatDelegate.MODE_NIGHT_NO ->
                    getString(R.string.settings_night_mode_no_title)
                else -> getString(R.string.settings_night_mode_follows_system_title)
            }
            val desc = when (it) {
                AppCompatDelegate.MODE_NIGHT_AUTO ->
                    getString(R.string.settings_night_mode_auto_desc)
                AppCompatDelegate.MODE_NIGHT_YES ->
                    getString(R.string.settings_night_mode_yes_desc)
                AppCompatDelegate.MODE_NIGHT_NO ->
                    getString(R.string.settings_night_mode_no_desc)
                else ->
                    getString(R.string.settings_night_mode_follows_system_desc)
            }

            container.addRadioItemView(
                title,
                desc,
                currentNightMode == it,
                View.OnClickListener { v ->
                    nightModeCallback?.onSelected(it)
                    dismiss()
                })
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        nightModeCallback?.onDismissed()
    }

    companion object {
        const val TAG = "night_mode_dialog"

        fun newInstance(
            currentNightMode: Int,
            nightModeCallback: NightModeCallback
        ): RoundedAlertDialog {
            val dialog = NightModeDialog()
            dialog.currentNightMode = currentNightMode
            dialog.nightModeCallback = nightModeCallback
            return dialog
        }
    }
}