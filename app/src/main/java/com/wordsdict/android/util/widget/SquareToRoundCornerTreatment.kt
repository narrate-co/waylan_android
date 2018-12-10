package com.wordsdict.android.util.widget

import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath

/**
 * A CornerTreatment that produces squared corners in its "default" state and rounds them as
 * interpolation decreases.
 *
 * @param maxCornerRadius The corner radius to draw when interpolation is 0.0
 */
class SquareToRoundCornerTreatment(
        private var maxCornerRadius: Float = 0F
) : CornerTreatment() {


    override fun getCornerPath(angle: Float, interpolation: Float, shapePath: ShapePath) {
        super.getCornerPath(angle, interpolation, shapePath)

        val roundedCornerOffset = (1F - interpolation) * maxCornerRadius

        shapePath.reset(0F, roundedCornerOffset)
        shapePath.addArc(
                0f,
                0f,
                roundedCornerOffset * 2,
                roundedCornerOffset * 2,
                180F,
                90F
        )

    }

}