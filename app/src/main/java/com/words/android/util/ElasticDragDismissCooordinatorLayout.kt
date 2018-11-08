package com.words.android.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutLinearInInterpolator

class ElasticDragDismissCooordinatorLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = androidx.coordinatorlayout.R.attr.coordinatorLayoutStyle
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = ElasticDragDismissCooordinatorLayout::class.java.simpleName
    }

    private var dragDismissDistance = Float.MAX_VALUE
    private var dragDismissFraction = -1F
    private val dragDismissScale = 1F
    private val shouldScale = false
    private val dragElacticity = 0.8F
    private var lastActionEvent: Int? = null

    private var draggingDown = false
    private var draggingUp = false

    private var totalDrag = 0F

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        //Are we scrolling vertically? If yes, claim the nested scroll by returning true
        return (nestedScrollAxes and View.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {

        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(dy)
            consumed[1] = dy
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        dragScale(dyUnconsumed)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        lastActionEvent = ev?.action
        return super.onInterceptTouchEvent(ev)
    }

    override fun onStopNestedScroll(target: View) {
        if (Math.abs(totalDrag) >= dragDismissDistance) {
            dispatchDismissCallback()
        } else {
            if (lastActionEvent == MotionEvent.ACTION_DOWN) {
                translationY = 0F
                scaleX = 1F
                scaleY = 1F
            } else {
                animate()
                        .translationY(0F)
                        .scaleX(1F)
                        .scaleY(1F)
                        .setDuration(200L)
                        .setInterpolator(FastOutLinearInInterpolator())
                        .setListener(null)
                        .start()
            }
            totalDrag = 0F
            draggingDown = false
            draggingUp = false

            dispatchDragCallback(0F, 0F, 0F, 0F)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (dragDismissFraction > 0F) {
            dragDismissDistance = h * dragDismissFraction
        }
    }

    private fun dragScale(scroll: Int) {
       if (scroll == 0) return

        totalDrag += scroll

        if (scroll < 0 && !draggingUp && !draggingDown) {
            draggingDown = true
            if (shouldScale) pivotY = height.toFloat()
        } else if (scroll > 0 && !draggingDown && !draggingUp) {
            draggingUp = true
            if (shouldScale) pivotY = 0F
        }

        var dragFraction: Float = Math.log10(((1 + (Math.abs(totalDrag) / dragDismissDistance)).toDouble())).toFloat()
        var dragTo: Float = dragFraction * dragDismissDistance * dragElacticity

        if (draggingUp) {
            dragTo *= -1
        }
        translationY = dragTo

        if (shouldScale) {
            val scale = 1 - ((1 - dragDismissScale) * dragFraction)
            scaleX = scale
            scaleY = scale
        }

        if ((draggingDown && totalDrag >= 0)
                || (draggingUp && totalDrag <= 0)) {
            totalDrag = 0F
            dragTo = 0F
            dragFraction = 0F
            draggingDown = false
            draggingUp = false
            translationY = 0F
            scaleX = 0F
            scaleY = 0F
        }

        dispatchDragCallback(dragFraction, dragTo, Math.min(1F, Math.abs(totalDrag) / dragDismissDistance), totalDrag)
    }

    private fun dispatchDragCallback(elasticOffset: Float, elasticOffsetPixels: Float, rawOffset: Float, rawOffsetPixels: Float) {
        println("$TAG::dispatchDragCallback")
        //TODO add dispatch drag callback
    }

    private fun dispatchDismissCallback() {
        println("$TAG::dispatchDismissCallback")
        //TODO add dismiss callback
    }
}