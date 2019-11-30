package space.narrate.waylan.core.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes

/**
 * Get the [ColorStateList] from the given [index]. If the index is null, return a ColorStateList
 * of [defColor].
 */
fun TypedArray.getColorStateList(
    context: Context,
    @StyleableRes index: Int,
    @AttrRes defColor: Int
): ColorStateList {
    return getColorStateList(index) ?: ColorStateList.valueOf(context.getColorFromAttr(defColor))
}