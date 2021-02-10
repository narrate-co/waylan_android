package space.narrate.waylan.settings.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.visible
import space.narrate.waylan.settings.R

class MessageAlertDialog(
    private val context: Context,
    @StringRes private val message: Int,
    @StringRes private val positiveButton: Int? = null,
    private val positiveAction: () -> Unit = {},
    @StringRes private val negativeButton: Int? = null,
    private val negativeAction: () -> Unit = {}
) {

    private val builder = MaterialAlertDialogBuilder(context)
        .setView(createMessageView())

    private var dialog: AlertDialog? = null

    fun show() {
        dialog = builder.show()
    }

    private fun createMessageView(): ViewGroup {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_message_view, null, false)
        val messageView = view.findViewById<TextView>(R.id.message)
        val positiveButtonView = view.findViewById<Button>(R.id.positive_button)
        val negativeButtonView = view.findViewById<Button>(R.id.negative_button)

        messageView.setText(message)
        setActionButton(positiveButtonView, positiveButton, positiveAction)
        setActionButton(negativeButtonView, negativeButton, negativeAction)
        return view as ViewGroup
    }

    private fun setActionButton(button: Button, @StringRes text: Int?, action: () -> Unit) {
        if (text == null) {
            button.gone()
        } else {
            button.visible()
            button.setText(text)
            button.setOnClickListener { action() }
        }
    }
}