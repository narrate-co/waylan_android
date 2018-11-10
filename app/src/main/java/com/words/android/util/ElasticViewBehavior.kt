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

        private const val DIRECTION_UP = 1
        private const val DIRECTION_DOWN = 2
        private const val DIRECTION_RIGHT = 3
        private const val DIRECTION_LEFT = 4
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



    // [dragDismissDistance] and [dragDismissFraction] are mutually exclusive. If you set dragDismissDistance,
    // this hard coded value will be used to determine what is considered a dismissal. If you set dragDismissFraction
    // the drag distance needed for a dismissal will be recalculated based on the CoordinatorLayout's height.
    // Prefer dragDismissFraction as this words better across screen sizes and configuration/layout changes
    // The scroll amount needed to trigger a dismissal
    private var dragDismissDistance =  300F //200F
    // The fraction of the total height of the CoordinatorLayout that should trigger a dismissal event
    // An easy way to think about this is "What % of the screen (How far) does a user's thumb need to scroll to dismiss this view?"
    private var dragDismissFraction = 0.15F

    // The percentage of totalDrag by which view properties should be animated
    // Higher values equate to a more 1-to-1 animation/scrolling
    private var dragElasticity = .35F

    // How intensely to scale the parent CoordinatorLayout at the end of [dragDismissDistance]. 1F == none, 0F == completely gone
    private var dragDismissScale = 1F
    private var shouldScale = false


    /** Drag variables */
    private var lastActionEvent: Int? = null
    // True if we're consuming scroll events and the initial direction was DOWN
    private var draggingDown = false
    // True if we're consuming scroll events and the initial direction was UP
    private var draggingUp = false
    // Holder for total drag accumulation. Negative indicates draggingDown. Positive indicates draggingUp
    private var totalDrag = 0F
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
            }
            // Set dragDismissFraction/dragDismissDistance
            if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissFraction)) {
                dragDismissFraction = a.getFloat(R.styleable
                        .ElasticViewBehavior_dragDismissFraction, dragDismissFraction)
            } else if (a.hasValue(R.styleable.ElasticViewBehavior_dragDismissDistance)) {
                dragDismissDistance = a.getDimensionPixelSize(R.styleable
                        .ElasticViewBehavior_dragDismissDistance, dragDismissDistance.toInt()).toFloat()
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
        val cornerTreatment = SquareToRoundCornerTreatment(context!!.resources.getDimension(R.dimen.drag_max_corner_radius))
        materialShapeDrawable.shapedViewModel?.topRightCorner = cornerTreatment
        materialShapeDrawable.shapedViewModel?.topLeftCorner = cornerTreatment
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
            dragDismissDistance = parent.height * dragDismissFraction
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        lastActionEvent = ev.action
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    /**
     * Called once when initiating a scrolling event
     *
     * Returning true indicates that we want to receive subsequet scroll events in [onNestedPreScroll] and [onNestedScroll]
     */
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        println("$TAG::onStartNestedScroll")
        return (axes and View.SCROLL_AXIS_VERTICAL) != 0 && !flinging
    }


    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        println("$TAG::onNestedPreScroll - dy = $dy, draggingDown = $draggingDown, draggingUp = $draggingUp")
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(coordinatorLayout, dy)
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        println("$TAG::onNestedScroll - dyUnconsumed = $dyUnconsumed")
        dragScale(coordinatorLayout, dyUnconsumed)
    }

    /**
     * If a fling is going to happen and we are not currently consuming scrolling, don't start
     */
    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        println("$TAG::onNestedPreFling - draggingUp = $draggingUp, draggingDown = $draggingDown")
        if (!draggingUp && !draggingDown) { //we are not currently dragging and should not consume this fling
            flinging = true
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        println("$TAG::onNestedFling")
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        println("$TAG::onStopNestedScroll - totalDrag = $totalDrag, dragDismissDistance = $dragDismissDistance")
        if (Math.abs(totalDrag) >= dragDismissDistance) {
            dispatchDismissCallback()
        } else {
            if (lastActionEvent == MotionEvent.ACTION_DOWN) {
                resetProperties(coordinatorLayout)
            } else {
                resetPropertiesByAnimation(coordinatorLayout)
            }

            totalDrag = 0F
            draggingDown = false
            draggingUp = false
            flinging = false

            dispatchDragCallback(0F, 0F, 0F, 0F)
        }
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        println("$TAG::onDependentViewChanged")
        if (dragDismissFraction > 0F) {
            dragDismissDistance = parent.height * dragDismissFraction
        }

        return super.onDependentViewChanged(parent, child, dependency)
    }


    /**
     * @param scroll 0 is resting. Negative values are dragging down. Positive values are dragging up
     */
    private fun dragScale(view: View, scroll: Int) {
       if (scroll == 0) return

        totalDrag += scroll

        // if this is a new scroll event, both draggingUp and draggingDown should be false
        // Determine which direction we are scrolling and set the appropriate boolean
        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true
            if (shouldScale) view.pivotY = view.height.toFloat()
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true
            if (shouldScale) view.pivotY = 0F
        }

        println("$TAG::dragScale totalDrag = $totalDrag, dragDismissDistance = $dragDismissDistance, draggingDown = $draggingDown, draggingUp = $draggingUp")

        var dragFraction: Float = Math.log10(((1 + (Math.abs(totalDrag) / dragDismissDistance)).toDouble())).toFloat()
        var dragTo: Float = dragFraction * dragDismissDistance * dragElasticity

        if (draggingUp) {
            dragTo *= -1
        }

        scaleProperties(view, dragFraction, dragTo)


        if (
                (draggingDown && totalDrag >= 0) // if dragging down, totalDrag should always be negative
                || (draggingUp && totalDrag <= 0) // if dragging up, totalDrag should always be positive
                || (draggingUp && !shouldDismissUp) // if should not dismiss up, ignore draggingUp events
                || (draggingDown && !shouldDismissDown) // if should not dismiss down, ignore draggingDown events
        ) {
            totalDrag = 0F
            dragTo = 0F
            dragFraction = 0F
            draggingDown = false
            draggingUp = false

            resetProperties(view)
        }

        dispatchDragCallback(dragFraction, dragTo, Math.min(1F, Math.abs(totalDrag) / dragDismissDistance), totalDrag)
    }


    private fun scaleProperties(view: View, dragFraction: Float, dragTo: Float) {
        view.translationY = dragTo

        val scale = 1 - ((1 - dragDismissScale) * dragFraction)
        val interp = 1 - dragFraction

        materialShapeDrawable.interpolation = interp

        if (shouldScale) {
            view.scaleX = scale
            view.scaleY = scale
        }
    }

    private fun resetProperties(view: View) {
        view.translationY = 0F
        view.scaleX = 1F
        view.scaleY = 1F
        materialShapeDrawable.interpolation = 1F
    }

    private fun resetPropertiesByAnimation(view: View) {
         view.animate()
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