package com.words.android.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.TransitionValues
import com.words.android.R


class ElasticTransition(private val up: Boolean) : Fade() {

    companion object {
        private const val TAG = "ElasticTransition"

        private const val PROPNAME_TRANSLATION_Y = "com.words.android.ExitTransition:translationY"

        private const val TRANSLATION_DISTANCE = 200F
    }


    override fun captureStartValues(transitionValues: TransitionValues) {
        val coordinatorLayout: CoordinatorLayout? = transitionValues.view.findViewById(R.id.coordinator)
        transitionValues.values[PROPNAME_TRANSLATION_Y] = coordinatorLayout?.translationY ?: 0F
        super.captureStartValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        val coordinatorLayout: CoordinatorLayout? = transitionValues.view.findViewById(R.id.coordinator)
        transitionValues.values[PROPNAME_TRANSLATION_Y] = coordinatorLayout?.translationY ?: 0F
        super.captureEndValues(transitionValues)
    }


    override fun onAppear(sceneRoot: ViewGroup?, view: View?, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        println("$TAG::onAppear")
        val coordinatorLayout: CoordinatorLayout? = view?.findViewById(R.id.coordinator) ?: return null

        val set = AnimatorSet()

        val coordinatorAnim = ObjectAnimator.ofFloat(
                coordinatorLayout,
                "translationY",
                TRANSLATION_DISTANCE,
                0F
        )
        coordinatorAnim.duration = 200

        val scrimAnim = ObjectAnimator.ofFloat(
                view,
                "alpha",
                0F,
                1F
        )
        scrimAnim.duration = 100

        set.playTogether(coordinatorAnim, scrimAnim)
        return set
    }

    override fun onDisappear(sceneRoot: ViewGroup?, view: View?, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        println("$TAG::onDisappear")
        val coordinatorLayout: CoordinatorLayout? = view?.findViewById(R.id.coordinator) ?: return null

        val startY = startValues?.values?.get(PROPNAME_TRANSLATION_Y) as? Float ?: 0F
        val endY = startY + TRANSLATION_DISTANCE

        val set = AnimatorSet()

        val coordinatorAnim = ObjectAnimator.ofFloat(
                coordinatorLayout,
                "translationY",
                startY,
                endY
        )
        coordinatorAnim.duration = 200

        val scrimAnim = ObjectAnimator.ofFloat(
                view,
                "alpha",
                1F,
                0F
        )
        scrimAnim.duration = 100

        set.playTogether(coordinatorAnim, scrimAnim)
        return set
    }


}