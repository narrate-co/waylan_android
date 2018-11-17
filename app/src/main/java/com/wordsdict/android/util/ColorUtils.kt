package com.wordsdict.android.util

import android.content.Context
import android.util.TypedValue

fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}
//
//fun Int.darken(factor: Float): Int {
//    val a = Color.alpha(this)
//    val r = Color.red((this * factor).toInt())
//    val g = Color.green((this * factor).toInt())
//    val b = Color.blue((this * factor).toInt())
//    return Color.argb(
//            a,
//            Math.min(r, 255),
//            Math.min(g, 255),
//            Math.min(b, 255)
//    )
//}
