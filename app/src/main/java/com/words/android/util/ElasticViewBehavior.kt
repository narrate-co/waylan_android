package com.words.android.util

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapePathModel
import com.words.android.R

/**
 * A [CoordinatorLayout.Behavior] to handle elastic dragging of the parent [CoordinatorLayout]
 *
 * This behavior can be set on any child of the CoordinatorLayout
 *
 * It will do the following:
 *
 * - Pick up the [backgroundTint] property of the [CoordinatorLayout] and set a new [MaterialShapeDrawable]
 *   background for the CoordinatorLayout (to be used to animate corners and edges on elastic scrolling).
 *   This value will default to android.R.attr.windowBackground if none is set.
 *
 *
 *
 */
class ElasticViewBehavior<V : View> @JvmOverloads constructor(
        context: Context? = null,
        attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<V>(context, attrs) {

    companion object {
        private val TAG = ElasticViewBehavior::class.java.simpleName

        private const val DIRECTION_UP = 0x1
        private const val DIRECTION_DOWN = 0x2
        private const val DIRECTION_RIGHT = 0x4
        private const val DIRECTION_LEFT = 0x8

        private const val TOUCH_DRAG_THRESHOLD = 15F
    }

    interface ElasticViewBehaviorCallback {

        fun onDrag(dragFraction: Float, dragTo: Float, rawOffset: Float, rawOffsetPixels: Float, dragDismissScale: Float)

        /**
         * @return Whether the dismiss has been consumed. If true, the callback will be removed from
         * [callbacks]. Return true if you're popping a fragment or finishing an activity and want
         * to avoid any future callbacks in your onDragDismissed() method.
         */
        fun onDragDismissed(): Boolean
    }

    private val callbacks: MutableList<ElasticViewBehaviorCallback> = mutableListOf()

    /** Drag attributes */
    // Which directions the CoordinatorLayout should respond to dismissal interactions
    private var shouldDismissUp = false
    private var shouldDismissDown = true
    private var shouldDismissRight = false
    private var shouldDismissLeft = false



    // [dragDismissDistanceVertical] and [dragDismissFraction] are mutually exclusive. If you set dragDismissDistanceVertical,
    // this hard coded value will be used to determine what is considered a dismissal. If you set dragDismissFraction
    // the drag distance needed for a dismissal will be recalculated based on the CoordinatorLayout's height.
    // Prefer dragDismissFraction as this words better across screen sizes and configuration/layout changes
    // The scroll amount needed to trigger a dismissal
    private var dragDismissDistanceVertical =  300F //200F
    // The fraction of the total height of the CoordinatorLayout that should trigger a dismissal event
    // An easy way to think about this is "What % of the screen (How far) does a user's thumb need to scroll to dismiss this view?"
    private var dragDismissFraction = 0.15F

    private var dragDismissDistanceHorizontal = 300F

    // The percentage of totalDragY by which view properties should be animated
    // Higher values equate to a more 1-to-1 animation/scrolling
    private var dragElasticity = .35F

    // How intensely to scale the parent CoordinatorLayout at the end of [dragDismissDistanceVertical]. 1F == none, 0F == completely gone
    private var dragDismissScale = 1F
    private var shouldScale = false


    /** Drag variables */
    private var lastMotionEvent: MotionEvent? = null
    private var touchDragTotalX: Float = 0F
    // True if we're consuming scroll events and the initial direction was DOWN
    private var draggingDown = false
    // True if we're consuming scroll events and the initial direction was UP
    private var draggingUp = false
    // Holder for total drag accumulation. Negative indicates draggingDown. Positive indicates draggingUp
    private var totalDragY = 0F

    private var draggingRight = false
    private var draggingLeft = false
    private var totalDragX = 0F


    // Variable to determine if a scroll event calling onStartNestedScroll has come from a fling
    private var flinging = false

    // Background to use for the parent CoordinatorLayout. Used to animate edge and corner
    // shape with drag values
    private val materialShapeDrawable = MaterialShapeDrawable(ShapePathModel())

    init {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.ElasticViewBehavior, 0, 0)
        if (a != null) {
            if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissDirection)) {
                val dir = a.getInteger(R.styleable.ElasticViewBehavior_dragDismissDirection, DIRECTION_DOWN)
                //TODO add logic for horizontal (x-axes) scroll/fling events
                shouldDismissUp = dir.containsFlag(DIRECTION_UP)
                shouldDismissDown = dir.containsFlag(DIRECTION_DOWN)
                shouldDismissRight = dir.containsFlag(DIRECTION_RIGHT)
                shouldDismissLeft = dir.containsFlag(DIRECTION_LEFT)
                println("$TAG::dragDismissDirection = up = $shouldDismissUp, down = $shouldDismissDown, right = $shouldDismissRight, left = $shouldDismissLeft")
            }
            // Set dragDismissFraction/dragDismissDistanceVertical
            if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissFraction)) {
                dragDismissFraction = a.getFloat(R.styleable
                        .ElasticViewBehavior_dragDismissFraction, dragDismissFraction)
            } else if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissDistance)) {
                dragDismissDistanceVertical = a.getDimensionPixelSize(R.styleable
                        .ElasticViewBehavior_dragDismissDistance, dragDismissDistanceVertical.toInt()).toFloat()
            }

            if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissScale)) {
                dragDismissScale = a.getFloat(R.styleable
                        .ElasticViewBehavior_dragDismissScale, dragDismissScale)
                shouldScale = dragDismissScale != 1F
            }
            if (a.hasValue(R.styleable.ElasticViewBehavior_dragElasticity)) {
                dragElasticity = a.getFloat(R.styleable.ElasticViewBehavior_dragElasticity,
                        dragElasticity)
            }
        }
        a?.recycle()


        //TODO find a way to allow custom corner and edge shapes, set through xml
        val cornerTreatment = SquareToRoundCornerTreatment(context!!.resources.getDimension(R.dimen.elastic_view_behavior_max_corner_radius))
        materialShapeDrawable.shapedViewModel?.topRightCorner = cornerTreatment
        materialShapeDrawable.shapedViewModel?.topLeftCorner = cornerTreatment
        //TODO update to MDC 1.1.0 and use compat shadows to get shadows on the very top of this view
        // Shadows do not currently show due to how the Android framework handles its material light source.
        // Shadows are only rendered on the sides and bottom of views with a light source coming from the top of the screen
        materialShapeDrawable.isShadowEnabled = true
        materialShapeDrawable.shadowElevation = context.resources.getDimensionPixelSize(R.dimen.elastic_view_behavior_background_elevation)
        materialShapeDrawable.paintStyle = Paint.Style.FILL

        //TODO allow tagging other child views with custom attr to include them in property animations?
    }

    private fun Int.containsFlag(flag: Int): Boolean = (this or flag) == this

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        // Set the CoordinatorLayout's background
        val backgroundTint = parent.backgroundTintList ?: ColorStateList.valueOf(parent.context.getColorFromAttr(android.R.attr.windowBackground))
        DrawableCompat.setTintList(materialShapeDrawable, backgroundTint)
        ViewCompat.setBackground(parent, materialShapeDrawable)

        if (dragDismissFraction > 0F) {
            dragDismissDistanceVertical = parent.height * dragDismissFraction
            dragDismissDistanceHorizontal = parent.width * dragDismissFraction
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        println("$TAG::onInterceptTouchEvent - dx = ${ev.x}, dy = ${ev.y}, totalDragX = $totalDragX")

        lastMotionEvent = ev
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        return super.onTouchEvent(parent, child, ev)
    }

    /**
     * Called once when initiating a scrolling event
     *
     * Returning true indicates that we want to receive subsequet scroll events in [onNestedPreScroll] and [onNestedScroll]
     */
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        println("$TAG::onStartNestedScroll, scroll contains vertical = ${axes.containsFlag(View.SCROLL_AXIS_VERTICAL)}, horizontal = ${axes.containsFlag(View.SCROLL_AXIS_HORIZONTAL)}")
        if (flinging) return false

        if ((axes and View.SCROLL_AXIS_VERTICAL) != 0 && (shouldDismissDown || shouldDismissUp)) {
            return true
        } else if (axes.containsFlag(View.SCROLL_AXIS_HORIZONTAL) && (shouldDismissRight || shouldDismissLeft)) {
            return true
        } else {
            return false
        }


//        return (axes and View.SCROLL_AXIS_VERTICAL) != 0 && !flinging
    }


    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        println("$TAG::onNestedPreScroll - dy = $dy, draggingDown = $draggingDown, draggingUp = $draggingUp")
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScaleVertical(coordinatorLayout, dy)
            consumed[1] = dy
        }
        if (draggingRight && dx > 0 || draggingLeft && dy < 0) {
            dragScaleHorizontal(coordinatorLayout, dx)
            consumed[0] = dx
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        println("$TAG::onNestedScroll - dyUnconsumed = $dyUnconsumed")
        dragScaleVertical(coordinatorLayout, dyUnconsumed)
        dragScaleHorizontal(coordinatorLayout, dxUnconsumed)
    }

    /**
     * If a fling is going to happen and we are not currently consuming scrolling, don't start
     */
    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        println("$TAG::onNestedPreFling - draggingUp = $draggingUp, draggingDown = $draggingDown")
        if (!draggingUp && !draggingDown && !draggingRight && !draggingLeft) { //we are not currently dragging and should not consume this fling
            flinging = true
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        println("$TAG::onNestedFling")
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        println("$TAG::onStopNestedScroll - totalDragY = $totalDragY, dragDismissDistanceVertical = $dragDismissDistanceVertical")
        if (Math.abs(totalDragY) >= dragDismissDistanceVertical || Math.abs(totalDragX) >= dragDismissDistanceHorizontal) {
            dispatchDismissCallback()
        } else {
            if (lastMotionEvent?.action == MotionEvent.ACTION_DOWN) {
                resetProperties(coordinatorLayout)
            } else {
                resetPropertiesByAnimation(coordinatorLayout)
            }

            totalDragY = 0F
            draggingDown = false
            draggingUp = false

            totalDragX = 0F
            draggingRight = false
            draggingLeft = false

            flinging = false

            dispatchDragCallback(0F, 0F, 0F, 0F)
        }
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        println("$TAG::onDependentViewChanged")
        if (dragDismissFraction > 0F) {
            dragDismissDistanceVertical = parent.height * dragDismissFraction
            dragDismissDistanceHorizontal = parent.width * dragDismissFraction
        }

        return super.onDependentViewChanged(parent, child, dependency)
    }


    /**
     * @param scroll 0 is resting. Negative values are dragging down. Positive values are dragging up
     */
    private fun dragScaleVertical(view: View, scroll: Int) {
       if (scroll == 0) return

        totalDragY += scroll

        // if this is a new scroll event, both draggingUp and draggingDown should be false
        // Determine which direction we are scrolling and set the appropriate boolean
        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true
            if (shouldScale) view.pivotY = view.height.toFloat()
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true
            if (shouldScale) view.pivotY = 0F
        }

        println("$TAG::dragScaleVertical totalDragY = $totalDragY, dragDismissDistanceVertical = $dragDismissDistanceVertical, draggingDown = $draggingDown, draggingUp = $draggingUp")

        var dragFractionY: Float = Math.log10(((1 + (Math.abs(totalDragY) / dragDismissDistanceVertical)).toDouble())).toFloat()
        var dragToY: Float = dragFractionY * dragDismissDistanceVertical * dragElasticity

        if (draggingUp) {
            dragToY *= -1
        }

        scalePropertiesY(view, dragFractionY, dragToY)


        if (
                (draggingDown && totalDragY >= 0) // if dragging down, totalDragY should always be negative
                || (draggingUp && totalDragY <= 0) // if dragging up, totalDragY should always be positive
                || (draggingUp && !shouldDismissUp) // if should not dismiss up, ignore draggingUp events
                || (draggingDown && !shouldDismissDown) // if should not dismiss down, ignore draggingDown events
        ) {
            totalDragY = 0F
            dragToY = 0F
            dragFractionY = 0F
            draggingDown = false
            draggingUp = false

            resetProperties(view)
        }

        dispatchDragCallback(dragFractionY, dragToY, Math.min(1F, Math.abs(totalDragY) / dragDismissDistanceVertical), totalDragY)
    }

    private fun dragScaleHorizontal(view: View, scroll: Int) {
        if (scroll == 0) return

        totalDragX += scroll

        if (scroll < 0 && !draggingLeft && !draggingRight) {
            draggingRight = true
            if (shouldScale) view.pivotX = view.width.toFloat()
        } else if (scroll > 0 && !draggingRight && !draggingLeft) {
            draggingLeft = true
            if (shouldScale) view.pivotX = 0F
        }

        var dragFractionX: Float = Math.log10(((1 + (Math.abs(totalDragX) / dragDismissDistanceHorizontal)).toDouble())).toFloat()
        var dragToX: Float = dragFractionX * dragDismissDistanceHorizontal * dragElasticity

        if (draggingLeft) {
            dragToX *= -1
        }

        scalePropertiesX(view, dragFractionX, dragToX)

        if (
                (draggingRight && totalDragX >= 0)
            || (draggingLeft && totalDragX <= 0)
            || (draggingLeft && !shouldDismissLeft)
            || (draggingRight && !shouldDismissRight)
                ) {
            totalDragX = 0F
            dragToX = 0F
            dragFractionX = 0F
            draggingRight = false
            draggingLeft = false

            resetProperties(view)
        }

        //TODO dispatch drag event
    }


    private fun scalePropertiesY(view: View, dragFraction: Float, dragTo: Float) {
        view.translationY = dragTo

        val scale = 1 - ((1 - dragDismissScale) * dragFraction)
        val interp = 1 - dragFraction

        materialShapeDrawable.interpolation = interp

        if (shouldScale) {
            view.scaleX = scale
            view.scaleY = scale
        }
    }

    private fun scalePropertiesX(view: View, dragFraction: Float, dragTo: Float) {
        view.translationX = dragTo
    }

    private fun resetProperties(view: View) {
        view.translationX = 0F
        view.translationY = 0F
        view.scaleX = 1F
        view.scaleY = 1F
        materialShapeDrawable.interpolation = 1F
    }

    private fun resetPropertiesByAnimation(view: View) {
        view.animate()
                .translationX(0F)
                .translationY(0F)
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(200L)
                .setInterpolator(FastOutLinearInInterpolator())
                .setListener(null)
                .start()

        val valueAnim = ValueAnimator.ofFloat(materialShapeDrawable.interpolation, 1F)
        valueAnim.addUpdateListener {
            materialShapeDrawable.interpolation = it.animatedValue as Float
        }
        valueAnim.interpolator = FastOutLinearInInterpolator()
        valueAnim.duration = 200
        valueAnim.start()
    }

    fun addCallback(callback: ElasticViewBehaviorCallback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: ElasticViewBehaviorCallback) {
        callbacks.remove(callback)
    }

    private fun dispatchDragCallback(elasticOffset: Float, elasticOffsetPixels: Float, rawOffset: Float, rawOffsetPixels: Float) {
        callbacks.forEach { it.onDrag(elasticOffset, elasticOffsetPixels, rawOffset, rawOffsetPixels, dragDismissScale) }
    }

    private fun dispatchDismissCallback() {
        callbacks.removeAll {
            it.onDragDismissed()
        }
    }
}