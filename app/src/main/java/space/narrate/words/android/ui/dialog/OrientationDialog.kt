package space.narrate.words.android.ui.dialog

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import space.narrate.words.android.data.prefs.Orientation

/**
 * A [RoundedAlertDialog] that displays a list of radio button items, one for each
 * possible orientation preference option.
 */
class OrientationDialog: RoundedAlertDialog() {

    companion object {
        fun newInstance(orientation: Orientation, orientationCallback: OrientationCallback): RoundedAlertDialog {
            val dialog = OrientationDialog()
            dialog.currentOrientation = orientation
            dialog.orientationCallback = orientationCallback
            return dialog
        }
    }

    abstract class OrientationCallback {
        abstract fun onSelected(orientation: Orientation)
        abstract fun onDismissed()
    }

    private var currentOrientation: Orientation = Orientation.UNSPECIFIED
    private var orientationCallback: OrientationCallback? = null

    /**
     * Add all [Orientation]s to the [RoundedAlertDialog]'s container to be displayed
     */
    override fun setBuilderView(container: ViewGroup) {
        Orientation.values().forEach { or ->
            container.addRadioItemView(
                    getString(or.title),
                    getString(or.desc),
                    or == currentOrientation,
                    View.OnClickListener {
                        orientationCallback?.onSelected(or)
                        dismiss()
                    }
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        orientationCallback?.onDismissed()
    }
}