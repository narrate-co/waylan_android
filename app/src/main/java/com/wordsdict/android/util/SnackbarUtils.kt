package com.wordsdict.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.snackbar.Snackbar
import com.wordsdict.android.R

/**
 * Helper method to style a [Snackbar] which contains content which is informative, descriptive
 * or accessory in nature.
 *
 * Call this method after [Snackbar.make] and before [Snackbar.show]
 *
 * @param abovePeekedSheet true if this sheet will be displayed on a screen that contains
 *  the [SearchFragment] and should be given additional vertical offset to show above the peeked
 *  bottom search sheet.
 */
fun Snackbar.configInformative(context: Context, abovePeekedSheet: Boolean): Snackbar {
    return config(SnackbarType.INFORMATIVE, context, abovePeekedSheet)
}

/**
 * Helper method to style a [Snackbar] which contains content about an error or invalid state.
 *
 * Call this method after [Snackbar.make] and before [Snackbar.show]
 *
 * @param abovePeekedSheet true if this sheet will be displayed on a screen that contains
 *  the [SearchFragment] and should be given additional vertical offset to show above the peeked
 *  bottom search sheet.
 */
fun Snackbar.configError(context: Context, abovePeekedSheet: Boolean): Snackbar {
    return config(SnackbarType.ERROR, context, abovePeekedSheet)
}

/**
 * Possible ways in which a Words [Snackbar] can be styled
 */
private enum class SnackbarType {
    INFORMATIVE, ERROR
}

/**
 * Given a Snackbar, alter it's background, message text appearance, button text appearance and
 * margins to create a floating card with rounded corners.
 */
@SuppressLint("ResourceType")
private fun Snackbar.config(
        type: SnackbarType,
        context: Context,
        abovePeekedSheet: Boolean
): Snackbar {
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
                R.attr.drawableSnackbarBackground
        )
    }

    val a = context.theme.obtainStyledAttributes(attrs)

    val background = a.getDrawable(0)
    val textAppearance = R.style.TextAppearance_Words_Body1
    val buttonTextAppearance = R.style.TextAppearance_Words_Button
    val textColor = when (type) {
        SnackbarType.ERROR -> context.getColorFromAttr(R.attr.colorPrimaryOnError)
        SnackbarType.INFORMATIVE -> context.getColorFromAttr(R.attr.colorPrimaryOnDefault)
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

