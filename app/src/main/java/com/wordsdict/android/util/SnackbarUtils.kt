package com.wordsdict.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.snackbar.Snackbar
import com.wordsdict.android.R

fun Snackbar.configInformative(context: Context, abovePeekedSheet: Boolean): Snackbar {
    return config(SnackbarType.INFORMATIVE, context, abovePeekedSheet)
}

fun Snackbar.configError(context: Context, abovePeekedSheet: Boolean): Snackbar {
    return config(SnackbarType.ERROR, context, abovePeekedSheet)
}

private enum class SnackbarType {
    INFORMATIVE, ERROR
}

@SuppressLint("ResourceType")
private fun Snackbar.config(type: SnackbarType, context: Context, abovePeekedSheet: Boolean): Snackbar {
    val params = view.layoutParams as ViewGroup.MarginLayoutParams
    val keyline3 = context.resources.getDimensionPixelSize(R.dimen.keyline_3)
    val bottomOffset = if (abovePeekedSheet) context.resources.getDimensionPixelOffset(R.dimen.search_min_peek_height) else 0
    params.setMargins(keyline3, keyline3 ,keyline3 , bottomOffset + (keyline3/2))
    view.layoutParams = params

    val attrs = when (type) {
        SnackbarType.ERROR -> intArrayOf(
                R.attr.drawableSnackbarErrorBackground
        )
        SnackbarType.INFORMATIVE -> intArrayOf(
                R.attr.drawableSnackbarInformativeBackground
        )
    }

    val a = context.theme.obtainStyledAttributes(attrs)

    val background = a.getDrawable(0)
    val textAppearance = when (type) {
        SnackbarType.ERROR -> R.style.TextAppearance_Words_Light_Body1 //black text
        SnackbarType.INFORMATIVE -> R.style.TextAppearance_Words_Light_Body1_Inverse //white text
    }
    val buttonTextAppearance = when (type) {
        SnackbarType.ERROR -> R.style.TextAppearance_Words_Light_Button //black text
        SnackbarType.INFORMATIVE -> R.style.TextAppearance_Words_Light_Body1_Inverse //white text
    }
    val textColor = when (type) {
        SnackbarType.ERROR -> context.getColorFromAttr(R.attr.textColorOnError)
        SnackbarType.INFORMATIVE -> context.getColorFromAttr(R.attr.textColorOnInformative)
    }

    a.recycle()

    view.background = background
    ViewCompat.setElevation(view, 6F)

    //alter text
    val tv = view.findViewById<TextView>(R.id.snackbar_text)
    TextViewCompat.setTextAppearance(tv, textAppearance)
    tv.setTextColor(textColor)
    tv.typeface = ResourcesCompat.getFont(context, R.font.source_sans_pro)

    //alter action
    val action = view.findViewById<TextView>(R.id.snackbar_action)
    TextViewCompat.setTextAppearance(action, buttonTextAppearance)
    action.setTextColor(textColor)
    action.typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_medium)
    action.isAllCaps = false

    return this
}

