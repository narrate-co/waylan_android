package space.narrate.words.android.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.ui.MainViewModel
import space.narrate.words.android.ui.widget.ElasticAppBarBehavior
import java.lang.IllegalArgumentException

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
    // Check if the drawable being set is the same as what is already present
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


fun AppBarLayout.setUpWithElasticBehavior(
    callback: ElasticAppBarBehavior.ElasticViewBehaviorCallback
) {
    val params = layoutParams as? CoordinatorLayout.LayoutParams
        ?: throw IllegalArgumentException(
            "AppBarLayout must be a child of CoordinatorLayout to setup with ElasticBehavior"
        )

    val behavior = params.behavior as? ElasticAppBarBehavior
        ?: throw IllegalArgumentException("AppBarLayout must use ElasticAppBarBehavior")

    behavior.addCallback(callback)
}

fun AppBarLayout.setUpWithElasticBehavior(
    currentDestination: String,
    sharedViewModel: MainViewModel,
    parallaxOnDrag: List<View>,
    alphaOnDrag: List<View>
) {
    val callback = object : ElasticAppBarBehavior.ElasticViewBehaviorCallback {
        override fun onDrag(
            dragFraction: Float,
            dragTo: Float,
            rawOffset: Float,
            rawOffsetPixels: Float,
            dragDismissScale: Float
        ) {
            val alpha = 1 - dragFraction
            val cutDragTo = dragTo * .15F

            parallaxOnDrag.forEach { it.translationY = cutDragTo }
            alphaOnDrag.forEach { it.alpha = alpha }
        }

        override fun onDragDismissed(): Boolean {
            return sharedViewModel.onDragDismissBackEvent(currentDestination)
        }
    }

    setUpWithElasticBehavior(callback)
}


