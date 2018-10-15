package com.words.android.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.get
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.words.android.R
import com.words.android.data.disk.wordset.Synonym

fun Synonym.toChip(context: Context, chipGroup: ChipGroup?, onClick: ((synonym: Synonym) -> Unit)? = null): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.details_chip_layout, chipGroup, false) as Chip
    chip.chipText = this.synonym
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun String.toRelatedChip(context: Context, chipGroup: ChipGroup?, onClick: ((word: String) -> Unit)? = null): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.details_related_chip_layout, chipGroup, false) as Chip
    val underlinedString = SpannableString(this)
    underlinedString.setSpan(UnderlineSpan(),0,this.length,0)
    chip.text = underlinedString
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun Activity.hideSoftKeyboard() {
    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    im.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
}

class ViewGroupChildIterator(private val viewGroup: ViewGroup): Iterator<View> {

    private var current: Int = 0

    override fun hasNext(): Boolean = viewGroup.childCount > current

    override fun next(): View {
        val i = current
        current++
        return viewGroup.getChildAt(i)
    }

}

val ViewGroup.children: Iterator<View>
    get() = ViewGroupChildIterator(this)

val Context.displayHeightPx: Int
    get() = resources.displayMetrics.heightPixels

val Activity.displayHeightDp: Float
    get() = resources.displayMetrics.heightPixels / resources.displayMetrics.density

fun TextView.setTextAppearanceCompat(context: Context, resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(resId)
    } else {
        setTextAppearance(context, resId)
    }
}