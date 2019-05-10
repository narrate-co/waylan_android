package space.narrate.words.android.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapePathModel
import kotlinx.android.synthetic.main.dialog_rounded_alert.view.*
import space.narrate.words.android.R
import space.narrate.words.android.util.children
import space.narrate.words.android.util.getColorFromAttr
import kotlinx.android.synthetic.main.radio_list_item_layout.view.*
import java.lang.IllegalStateException

/**
 * A helper [AppCompatDialogFragment] which creates a Dialog with rounded
 * corners and exposes a "container" ViewGroup which subclasses can add views to before
 * the dialog is displayed.
 */
abstract class RoundedAlertDialog: AppCompatDialogFragment() {

    /**
     * Subclasses provide an implementation of [setBuilderView] to add any view they wish to
     * be displayed in the dialog. [container] is a vertically oriented LinearLayout, meaning
     * views will be displayed in the order they are added.
     */
    abstract fun setBuilderView(container: ViewGroup)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) throw IllegalStateException("Dialog's activity cannot be null")

        val builder = AlertDialog.Builder(activity!!)

        val v = requireActivity().layoutInflater.inflate(R.layout.dialog_rounded_alert, null)

        setBuilderView(v.container)

        builder.setView(v)

        val d = builder.create()

        ViewCompat.setBackground(d.window.decorView, MaterialShapeDrawable(
            context, null, R.attr.alertDialogStyle, R.style.Widget_MaterialComponents_Button_TextButton_Dialog
        ))
//        d.window?.setBackgroundDrawable(
//                createBackgroundDrawable(requireActivity(), R.dimen.keyline_2, R.attr.colorSurface)
//        )

        return d
    }

    /**
     * Create and set a custom background with [MaterialShapeDrawable]
     */
    private fun createBackgroundDrawable(
            context: Context,
            cornerRadiusDimen: Int,
            backgroundColorAttr: Int
    ): MaterialShapeDrawable {
        // Create a corner treatment to be used on all 4 corners
        val cornerTreatment = RoundedCornerTreatment(
                context.resources.getDimensionPixelSize(cornerRadiusDimen).toFloat()
        )

        // Create a shape model defining our background
        val shapePathModel = ShapePathModel().apply {
            setAllCorners(cornerTreatment)
        }

        // Construct our background drawable
        return MaterialShapeDrawable(shapePathModel).apply {
            isShadowEnabled = true
            paintStyle = Paint.Style.FILL
            DrawableCompat.setTintList(
                    this@apply,
                    ColorStateList.valueOf(context.getColorFromAttr(backgroundColorAttr)))
        }
    }

    /**
     * A helper method common to multiple Words subclasses to inflate and add a list item
     * that contains a radio button, a title and a description
     */
    fun ViewGroup.addRadioItemView(
        title: String,
        desc: String,
        checked: Boolean,
        listener: View.OnClickListener
    ) {
        val item = requireActivity().layoutInflater.inflate(
            R.layout.radio_list_item_layout,
            this,
            false
        )
        item.radioTitle.text = title
        item.radioDesc.text = desc

        val checkedDrawable = ContextCompat.getDrawable(
            requireActivity(),
            R.drawable.ic_round_radio_checked_24px
        )
        val uncheckedDrawable = ContextCompat.getDrawable(
            requireActivity(),
            R.drawable.ic_round_check_circle_outline_24px
        )
        item.radioButton.buttonDrawable = if (checked) checkedDrawable else uncheckedDrawable
        item.setOnClickListener { clickedItem ->
            children.forEach { other ->
                other.radioButton.buttonDrawable = uncheckedDrawable
            }
            clickedItem.radioButton.buttonDrawable = checkedDrawable
            listener.onClick(clickedItem)
        }

        addView(item)
    }
}