package space.narrate.waylan.settings.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import space.narrate.waylan.settings.R
import space.narrate.waylan.core.R as coreR

class RadioGroupAlertDialog<T : RadioItemModel>(
    context: Context,
    items: List<T>
)  {

    private val builder = MaterialAlertDialogBuilder(context)
        .setView(createListView(context, items))

    private var dialog: AlertDialog? = null

    private var onItemSelected: (T) -> Boolean = { true }

    /**
     * Run [block] when an item is selected. [block] should return whether or not the dialog
     * should be dismissed.
     */
    fun onItemSelected(block: (T) -> Boolean): RadioGroupAlertDialog<T> {
        onItemSelected = block
        return this
    }

    private fun dispatchOnItemSelected(item: T) {
        if (onItemSelected(item)) {
            dialog?.dismiss()
        }
    }

    fun show() {
        dialog = builder.show()
    }

    private fun createListView(context: Context, items: List<T>): ViewGroup {
        val keyline3 = context.resources.getDimensionPixelOffset(coreR.dimen.keyline_3)
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(paddingLeft, paddingTop + keyline3, paddingRight, paddingBottom + keyline3)
        }

        val checkedDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.ic_round_radio_checked_24px
        )
        val uncheckedDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.ic_round_radio_outline_24px
        )

        items.forEach { item ->
            val itemView = LayoutInflater.from(context).inflate(
                R.layout.radio_list_item_layout,
                container,
                false
            )

            itemView.findViewById<AppCompatTextView>(R.id.radioTitle).text =
                context.getString(item.titleRes)
            itemView.findViewById<AppCompatTextView>(R.id.radioDesc).text =
                context.getString(item.descRes)

            val radioButton = itemView.findViewById<AppCompatRadioButton>(R.id.radioButton)
            radioButton.buttonDrawable = if (item.selected) {
                checkedDrawable
            } else {
                uncheckedDrawable
            }
            // TODO Extract unchecking and checking logic into different memeber-level function
            itemView.setOnClickListener {
                container.children.forEach { otherItem ->
                    otherItem.findViewById<AppCompatRadioButton>(R.id.radioButton).buttonDrawable =
                        uncheckedDrawable
                }
                radioButton.buttonDrawable = checkedDrawable
                dispatchOnItemSelected(item)
            }

            container.addView(itemView)
        }

        return container
    }


}