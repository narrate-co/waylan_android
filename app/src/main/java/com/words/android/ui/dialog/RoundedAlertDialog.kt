package com.words.android.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapePathModel
import com.words.android.R
import com.words.android.util.children
import com.words.android.util.getColorFromAttr
import kotlinx.android.synthetic.main.dialog_rounded_alert.view.*
import kotlinx.android.synthetic.main.radio_list_item_layout.view.*
import java.lang.IllegalStateException

class RoundedAlertDialog: AppCompatDialogFragment() {

    companion object {
        fun newNightModeInstance(currentNightMode: Int, nightModeCallback: NightModeCallback): RoundedAlertDialog {
            val dialog = RoundedAlertDialog()
            dialog.currentNightMode = currentNightMode
            dialog.nightModeCallback = nightModeCallback
            return dialog
        }
    }

    private var currentNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private var nightModeCallback: NightModeCallback? = null

    abstract class NightModeCallback {
        abstract fun onSelected(nightMode: Int)
        abstract fun onDismissed()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) throw IllegalStateException("Dialog's activity cannot be null")

        val builder = AlertDialog.Builder(activity!!)

        val v = activity!!.layoutInflater.inflate(R.layout.dialog_rounded_alert, null)

        //Add radio buttons
        listOf(
                AppCompatDelegate.MODE_NIGHT_AUTO,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO
        ).forEach {
            val item = activity!!.layoutInflater.inflate(R.layout.radio_list_item_layout, v.container, false)
            val title = when (it) {
                AppCompatDelegate.MODE_NIGHT_AUTO -> "Auto"
                AppCompatDelegate.MODE_NIGHT_YES -> "Yes"
                AppCompatDelegate.MODE_NIGHT_NO -> "No"
                else -> "Follow System"
            }
            val desc = when (it) {
                AppCompatDelegate.MODE_NIGHT_AUTO -> "Night mode turns on between sunset and sunrise"
                AppCompatDelegate.MODE_NIGHT_YES -> "Always use night mode"
                AppCompatDelegate.MODE_NIGHT_NO -> "Never use night mode"
                else -> "Follow System"
            }
            item.radioTitle.text = title
            item.radioDesc.text = desc


            val checkedDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_rounded_radio_checked_24px)
            val uncheckedDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_round_check_circle_outline_black_24px)

            item.radioButton.buttonDrawable = if (currentNightMode == it) checkedDrawable else uncheckedDrawable
            item.setOnClickListener {clickedItem ->
                v.container.children.forEach { other ->
                    other.radioButton.buttonDrawable = uncheckedDrawable
                }
                clickedItem.radioButton.buttonDrawable = checkedDrawable
                nightModeCallback?.onSelected(it)
                dismiss()
            }
            
            v.container.addView(item)
        }

        builder.setView(v)

        val d = builder.create()
        d.window.setBackgroundDrawable(createBackgroundDrawable(activity!!, R.dimen.keyline_2, R.attr.colorPrimary))

        return d
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        nightModeCallback?.onDismissed()
    }

    private fun createBackgroundDrawable(context: Context, cornerRadiusDimen: Int, backgroundColorAttr: Int): MaterialShapeDrawable {
        val shapePathModel = ShapePathModel().apply {
            setAllCorners(RoundedCornerTreatment(context.resources.getDimensionPixelSize(cornerRadiusDimen).toFloat()))
        }

        val materialShapeDrawable = MaterialShapeDrawable(shapePathModel).apply {
            isShadowEnabled = true
            paintStyle = Paint.Style.FILL
            DrawableCompat.setTintList(
                    this@apply,
                    ColorStateList.valueOf(context.getColorFromAttr(backgroundColorAttr)))
        }

        return materialShapeDrawable
    }
}