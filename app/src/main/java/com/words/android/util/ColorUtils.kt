package com.words.android.util

import android.content.Context
import android.util.TypedValue
import androidx.core.content.ContextCompat

fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}
