package com.wordsdict.android.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapePathModel
import com.wordsdict.android.R
import com.wordsdict.android.util.children
import com.wordsdict.android.util.getColorFromAttr
import kotlinx.android.synthetic.main.dialog_rounded_alert.view.*
import kotlinx.android.synthetic.main.radio_list_item_layout.view.*
import java.lang.IllegalStateException

abstract class RoundedAlertDialog: AppCompatDialogFragment() {

    abstract fun setBuilderView(container: ViewGroup)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) throw IllegalStateException("Dialog's activity cannot be null")

        val builder = AlertDialog.Builder(activity!!)

        val v = activity!!.layoutInflater.inflate(R.layout.dialog_rounded_alert, null)

        setBuilderView(v.container)

        builder.setView(v)

        val d = builder.create()
        //TODO should this be a surface C?
        d.window.setBackgroundDrawable(createBackgroundDrawable(activity!!, R.dimen.keyline_2, R.attr.surfaceBColor))

        return d
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

    fun ViewGroup.addRadioItemView(title: String, desc: String, checked: Boolean, listener: View.OnClickListener) {
        val item = activity!!.layoutInflater.inflate(R.layout.radio_list_item_layout, this, false)
        item.radioTitle.text = title
        item.radioDesc.text = desc

        val checkedDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_round_radio_checked_24px)
        val uncheckedDrawable = ContextCompat.getDrawable(activity!!, R.drawable.ic_round_check_circle_outline_24px)
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