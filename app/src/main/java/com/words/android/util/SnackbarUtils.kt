package com.words.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.words.android.R
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow

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
                R.attr.textAppearanceBody1,
                R.attr.textAppearanceButton,
                R.attr.drawableSnackbarErrorBackground
        )
        SnackbarType.INFORMATIVE -> intArrayOf(
                R.attr.textAppearanceBody1Inverse,
                R.attr.textAppearanceButtonInverse,
                R.attr.drawableSnackbarInformativeBackground
        )
    }

    val a = context.theme.obtainStyledAttributes(attrs)

    val textAppearanceBody1 = a.getResourceId(0, 0)
    val textAppearanceButton = a.getResourceId(1, 0)
    val background = a.getDrawable(2)

    a.recycle()


    view.background = background
    ViewCompat.setElevation(view, 6F)

    //alter text
    val tv = view.findViewById<TextView>(R.id.snackbar_text)
    tv.setTextAppearanceCompat(context, textAppearanceBody1)
    tv.typeface = ResourcesCompat.getFont(context, R.font.source_sans_pro)

    //alter action
    val action = view.findViewById<TextView>(R.id.snackbar_action)
    action.setTextAppearanceCompat(context, textAppearanceButton)
    action.typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_medium)
    action.isAllCaps = false

    return this
}

