package com.wordsdict.android.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.wordsdict.android.R
import com.wordsdict.android.data.disk.wordset.Synonym
import com.wordsdict.android.util.widget.DelayedLifecycleAction

fun Synonym.toChip(
        context: Context,
        chipGroup: ChipGroup?,
        onClick: ((synonym: Synonym) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
            R.layout.details_chip_layout,
            chipGroup,
            false
    ) as Chip
    chip.text = this.synonym
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun String.toRelatedChip(
        context: Context,
        chipGroup: ChipGroup?,
        onClick: ((word: String) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
            R.layout.details_related_chip_layout,
            chipGroup,
            false
    ) as Chip
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

fun Activity.showSoftKeyboard(view: View) {
    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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


fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.visible() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun ImageView.swapImageResource(imgRes: Int) {
    // Check if the drawbale being set is the same as what is already present
    val currantDrawableState = drawable.constantState
    val newDrawableState = ContextCompat.getDrawable(context, imgRes)?.constantState
    if (currantDrawableState == newDrawableState) return

    val alphaOut = ObjectAnimator.ofFloat(this, "alpha", 0F)
    alphaOut.duration = 100
    alphaOut.interpolator = AccelerateInterpolator()
    alphaOut.addListener(object: AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            setImageResource(imgRes)
        }
    })

    val alphaIn = ObjectAnimator.ofFloat(this, "alpha", 1F)
    alphaIn.duration = 100
    alphaIn.interpolator = DecelerateInterpolator()

    val set = AnimatorSet()
    set.playSequentially(alphaOut, alphaIn)
    set.start()
}