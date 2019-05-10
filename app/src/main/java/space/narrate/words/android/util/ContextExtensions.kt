package space.narrate.words.android.util

import android.content.Context
import android.util.TypedValue

fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.getDimensionPixelSizeFromAttr(attr: Int): Int {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    val dimen = a.getDimensionPixelSize(0, 0)
    a.recycle()
    return dimen
}

val Context.displayHeightPx: Int
    get() = resources.displayMetrics.heightPixels

val Context.displayHeightDp: Float
    get() = resources.displayMetrics.heightPixels / resources.displayMetrics.density

fun Context.getStringOrNull(res: Int?): String? {
    return if (res == null) null else getString(res)
}
