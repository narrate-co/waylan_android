package com.words.android.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageButton
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

fun AppCompatImageButton.setChecked(value: Boolean) {
    if (value) {
        setImageResource(R.drawable.ic_round_check_circle_24px)
    } else {
        setImageResource(R.drawable.ic_round_check_circle_outline_24px)
    }
}

fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.visible() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.runTransitionDrawable(duration: Int, reverse: Boolean = false, isCrossFadeEnabled: Boolean = true) {
    (background as? TransitionDrawable)?.let {
        it.isCrossFadeEnabled = isCrossFadeEnabled
        if (!reverse) {
            it.startTransition(duration)
        } else {
            it.reverseTransition(duration)
        }
    }
}