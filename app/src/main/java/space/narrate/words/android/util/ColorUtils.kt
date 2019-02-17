package space.narrate.words.android.util

import android.content.Context
import android.util.TypedValue

fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

