package com.wordsdict.android.ui.dialog

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import com.wordsdict.android.data.prefs.Orientation

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