package space.narrate.words.android.util.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.TransitionValues
import space.narrate.words.android.R


/**
 * A Transition that animates a Fragment with a root view that acts as a scrim and a single child
 * CoordinatorLayout holding its content.
 *
 * When a Fragment that is elastic is being dragged to dismiss, the Fragment below it often shares
 * the same windowBackground color, making the dragging Fragment and the Fragment beneath it's
 * border visually indistinguishable. To solve this problem, either a shadow along the top of the
 * elastic Fragment could be used or a scrim behind the elastic Fragment could be used. Due to the
 * way Android's material light source works, shadows near the top of the window are not visible
 * (the light source lives near the top of the window and points towards the bottom, casting shadows
 * along the bottom and sides of views but being basically non existent along the top of views when
 * near the top of the window). For this reason, Fragments that are elastic are wrapped in an
 * [FrameLayout] that acts as a scrim behind the Fragments main content.
 *
 * For this transition, we want to animate the elastic Fragment's scrim and main contents
 * seperately. This Transition requires the Elastic fragment to have a CoordinatorLayout with the
 * id R.id.coordinator as the only child of an [FrameLayout]. On enter, we animate the
 * translationY of the main contents CoordinatorLayout, creating the slide up animation and animate
 * the alpha of the [FrameLayout], effectively animating in the entire Fragment's view.
 */
class ElasticTransition : Fade() {

    companion object {
        private const val PROPNAME_TRANSLATION_Y =
                "space.narrate.words.android.ExitTransition:translationY"

        private const val DEFAULT_DURATION = 225L
        private const val TRANSLATION_DISTANCE = 200F
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
        super.captureStartValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
        super.captureEndValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val coordinatorLayout: CoordinatorLayout? =
                transitionValues.view.findViewById(R.id.coordinator_layout)
        transitionValues.values[PROPNAME_TRANSLATION_Y] = coordinatorLayout?.translationY ?: 0F
    }


    override fun onAppear(
            sceneRoot: ViewGroup?,
            view: View?,
            startValues: TransitionValues?,
            endValues: TransitionValues?
    ): Animator? {
        val coordinatorLayout: CoordinatorLayout? = view?.findViewById(R.id.coordinator_layout)
                ?: return null

        val set = AnimatorSet()

        // Animate the root view's translationY, creating the slide effect
        val coordinatorAnim = ObjectAnimator.ofFloat(
                coordinatorLayout,
                "translationY",
                TRANSLATION_DISTANCE,
                0F
        )
        coordinatorAnim.duration = DEFAULT_DURATION

        // Animate the alpha
        val scrimAnim = ObjectAnimator.ofFloat(
                view,
                "alpha",
                0F,
                1F
        )
        scrimAnim.duration = DEFAULT_DURATION

        set.playTogether(coordinatorAnim, scrimAnim)
        return set
    }

    override fun onDisappear(
            sceneRoot: ViewGroup?,
            view: View?,
            startValues: TransitionValues?,
            endValues: TransitionValues?
    ): Animator? {
        val coordinatorLayout: CoordinatorLayout? = view?.findViewById(R.id.coordinator_layout)
                ?: return null

        val startY = startValues?.values?.get(PROPNAME_TRANSLATION_Y) as? Float ?: 0F
        val endY = startY + TRANSLATION_DISTANCE

        val set = AnimatorSet()

        val coordinatorAnim = ObjectAnimator.ofFloat(
                coordinatorLayout,
                "translationY",
                startY,
                endY
        )
        coordinatorAnim.duration = DEFAULT_DURATION

        val scrimAnim = ObjectAnimator.ofFloat(
                view,
                "alpha",
                1F,
                0F
        )
        scrimAnim.duration = DEFAULT_DURATION / 2

        set.playTogether(coordinatorAnim, scrimAnim)
        return set
    }


}