package com.wordsdict.android.ui.settings

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.PointF
import android.graphics.Rect
import android.util.Property
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.Transition
import androidx.transition.TransitionValues
import com.wordsdict.android.R
import com.wordsdict.android.util.setLeftTopRightBottom


class ShelfTransition: Transition() {

    companion object {
        private const val PROPNAME_RECT = "com.wordsdict.rect"
        private const val PROPNAME_SHELF_HEIGHT = "com.wordsdict.ychange"
        private const val PROPNAME_PLACEHOLDER = "com.wordsdict.placeholder"
    }

    var rand = 0
    var startDiff = 0
    var endDiff = 0

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, true)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, false)
    }

    fun captureValues(values: TransitionValues, start: Boolean) {
        if (values.view.id == R.id.shelfContainer) {
            val diff = values.view.bottom - values.view.top
            values.values[PROPNAME_SHELF_HEIGHT] = values.view.bottom - values.view.top
            if (start) {
                startDiff = diff
            } else {
                endDiff = diff
            }
        }
        if (values.view is ConstraintLayout) {
            values.values[PROPNAME_RECT] = Rect(values.view.left, values.view.top, values.view.right, values.view.bottom)
            values.values[PROPNAME_PLACEHOLDER] = rand++
        }
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        val view = endValues?.view ?: return null
        if (view is ConstraintLayout && startValues != null && endValues != null) {
            val startRect = startValues.values[PROPNAME_RECT] as Rect
            val endRect = endValues.values[PROPNAME_RECT] as Rect

            val diff = endDiff - startDiff
            endRect.top -= diff
            endRect.bottom -= diff


            view.setLeftTopRightBottom(startRect.left, startRect.top, startRect.right, startRect.bottom)

            val leftTop = pathMotion.getPath(startRect.left.toFloat(), startRect.top.toFloat(), endRect.left.toFloat(), endRect.top.toFloat())
            val leftTopAnimator = ObjectAnimator.ofObject(sceneRoot, object: Property<View, PointF>(PointF::class.java, "topBottom") {
                override fun get(`object`: View?): PointF? {
                    return null
                }
                override fun set(view: View, leftTop: PointF) {
                    view.left  = leftTop.x.toInt()
                    view.top = leftTop.y.toInt()
                }
            }, null, leftTop)

            val bottomRight = pathMotion.getPath(startRect.right.toFloat(), startRect.bottom.toFloat(), endRect.right.toFloat(), endRect.bottom.toFloat())
            val bottomRightAnimator = ObjectAnimator.ofObject(sceneRoot, object : Property<View, PointF>(PointF::class.java, "bottomRight") {
                override fun get(`object`: View?): PointF? {
                    return null
                }

                override fun set(view: View, bottomRight: PointF) {
                    view.right = bottomRight.x.toInt()
                    view.bottom = bottomRight.y.toInt()
                }
            }, null, bottomRight)


            val set = AnimatorSet()
            set.playTogether(leftTopAnimator, bottomRightAnimator)
            return set
        }


        return super.createAnimator(sceneRoot, startValues, endValues)
    }

}