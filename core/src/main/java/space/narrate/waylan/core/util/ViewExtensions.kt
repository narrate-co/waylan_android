package space.narrate.waylan.core.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import space.narrate.waylan.core.R
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.ui.widget.ElasticAppBarBehavior
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.visible() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

val View.inflater: LayoutInflater
    get() = LayoutInflater.from(context)

fun <T : View> T.fadeThroughTransition(with: T.() -> Unit) {
    // Check if the drawable being set is the same as what is already present
    val alphaOut = ObjectAnimator.ofFloat(this, "alpha", 0F)
    alphaOut.duration = 100
    alphaOut.interpolator = AccelerateInterpolator()
    alphaOut.addListener(object: AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            with()
        }
    })

    val alphaIn = ObjectAnimator.ofFloat(this, "alpha", 1F)
    alphaIn.duration = 100
    alphaIn.interpolator = DecelerateInterpolator()

    val set = AnimatorSet()
    set.playSequentially(alphaOut, alphaIn)
    set.start()
}

fun String.toChip(
    context: Context,
    chipGroup: ChipGroup?,
    onClick: ((value: String) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
        R.layout.chip_layout,
        chipGroup,
        false
    ) as Chip
    chip.text = this
    chip.background.alpha = (0.2 * 255).toInt()
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

/**
 * A breadth first search of all descendants from [ViewGroup]. This returns the first View which
 * matches the given [predicate]. If no matching view is found, this method will return null.
 */
fun ViewGroup.findFirstDescendantOrNull(predicate: (v: View) -> Boolean): View? {
    val queue = PriorityQueue<ViewGroup>()
    queue.add(this)
    while (queue.isNotEmpty()) {
        queue.poll()?.children?.forEach {
            if (predicate(it)) {
                return it
            } else if (it is ViewGroup) {
                queue.add(it)
            }
        }
    }

    return null
}

fun View.hideIme() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController?.hide(WindowInsets.Type.ime())
    } else {
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = (context as? Activity)?.currentFocus ?: this
        im.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }
}

fun View.showIme() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
