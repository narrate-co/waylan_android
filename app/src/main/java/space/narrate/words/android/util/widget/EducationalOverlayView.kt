package space.narrate.words.android.util.widget

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import space.narrate.words.android.R
import space.narrate.words.android.util.getColorFromAttr

/**
 * A View that lays itself out full screen, attached to the root of the window (or close to it)
 * and can be used to draw over the screen, educating the user about features.
 *
 * TODO make this view more flexible, leaving it up to the static constructor helper functions
 * TODO to set up animations, etc
 */
class EducationalOverlayView private constructor(
        appBarLayout: AppBarLayout,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : View(appBarLayout.context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        /**
         * @param appBarLayout Must be an app bar layout with an [ElasticAppBarBehavior]
         */
        fun pullDownEducator(appBarLayout: AppBarLayout): EducationalOverlayView {
            return EducationalOverlayView(appBarLayout)
        }
    }

    // The view group which we will add this view to
    private var rootView: ViewGroup? = findRootView(appBarLayout)

    // The elastic behavior of the appBarLayout passed into pullDownEducator()
    private var elasticBehavior: ElasticAppBarBehavior =
        (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams)
                .behavior as ElasticAppBarBehavior

    // The paint used for the educational dot
    private var dotPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = context.getColorFromAttr(R.attr.colorPrimary)
        style = Paint.Style.FILL
    }


    private var rootViewHeight: Int = 0
    private var rootViewWidth: Int = 0

    private var dotDiameter =
            context.resources.getDimensionPixelSize(R.dimen.educational_overlay_dot_diameter)
    private var dotRect: RectF = RectF()
    private var topInset: Int = 0

    private var dotScale: Float = 0.0F
    private var dotTransY: Float = 0.0F


    private val showAnimator = ValueAnimator.ofFloat(0.0F, 1.0F).apply {
        duration = 200L
        interpolator = FastOutSlowInInterpolator()
        startDelay = 500L
        addUpdateListener {
            val interp = it.animatedValue as Float
            dotScale = interp
            dotPaint.alpha = (255 * interp).toInt()
            postInvalidateOnAnimation()
        }
    }

    private val loopAnimator = ValueAnimator.ofFloat(0.0F, 1.0F).apply {
        repeatMode = ValueAnimator.REVERSE
        repeatCount = 1
        duration = 1300L
        interpolator = FastOutSlowInInterpolator()
        addUpdateListener {
            val interp = it.animatedValue as Float
            elasticBehavior.simulateDragDown(interp)
            dotTransY = elasticBehavior.dragDismissDistanceVertical * interp
            postInvalidateOnAnimation()
        }
    }

    private val hideAnimator = ValueAnimator.ofFloat(1.0F, 0.0F).apply {
        duration = 200L
        interpolator = FastOutSlowInInterpolator()
        startDelay = 200L
        addUpdateListener {
            val interp = it.animatedValue as Float
            dotPaint.alpha = (255 * interp).toInt()
            dotScale = interp
            postInvalidateOnAnimation()
        }
    }

    init {
        layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        //TODO make sure insets are being passed through InsetFrameLayout properly and this
        //TODO callback is receiving accurate numbers
        setOnApplyWindowInsetsListener { _, insets ->
            topInset = insets.stableInsetTop
            calculateScreenLocations()
            insets
        }
    }

    fun show() {
        rootView?.addView(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rootViewHeight = h
        rootViewWidth = w
        calculateScreenLocations()

        val set = AnimatorSet()
        set.playSequentially(showAnimator, loopAnimator, hideAnimator)
        set.doOnEnd {
            rootView?.removeView(this)
        }
        set.start()
    }

    private fun calculateScreenLocations() {
        val dotLeft = (rootViewWidth / 2) - (dotDiameter / 2)
        val dotTop = (dotDiameter * 2) + topInset
        dotRect = RectF(
                dotLeft.toFloat(),
                dotTop.toFloat(),
                (dotLeft + dotDiameter).toFloat(),
                (dotTop + dotDiameter).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        // draw the dot
        val checkpoint = canvas.save()

        canvas.scale(
                dotScale,
                dotScale,
                dotRect.centerX(),
                dotRect.centerY()
        )
        canvas.translate(
                0F,
               dotTransY
        )

        canvas.drawOval(dotRect, dotPaint)

        canvas.restoreToCount(checkpoint)
    }


    private fun findRootView(v: View): ViewGroup? {
        var currentView: View? = v

        while (currentView?.parent != null && currentView.parent is View) {
            // InsetFrameLayout is only used for a root of a fragment. exit early
            if (currentView is InsetFrameLayout) return currentView
            currentView = currentView.parent as View
        }

        // we're assuming the root view will always be a ViewGroup?
        return if (currentView is ViewGroup) currentView else null
    }

}