package space.narrate.waylan.core.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.elevation.ElevationOverlayProvider

@BindingAdapter(
    "fadeBackgroundAngle",
    "fadeBackgroundElevation",
    requireAll = false
)
fun View.bindFadeBackground(
    fadeBackgroundAngle: Int,
    fadeBackgroundElevation: Float = 0F
) {
    val toColor = ElevationOverlayProvider(context)
        .compositeOverlayWithThemeSurfaceColorIfNeeded(fadeBackgroundElevation)
    val gradientDrawable = GradientDrawable(
        getGradientDrawableOrientationFromAngle(fadeBackgroundAngle),
        intArrayOf(Color.TRANSPARENT, toColor)
    )
    background = gradientDrawable
}

private fun getGradientDrawableOrientationFromAngle(angle: Int): GradientDrawable.Orientation {
    return when (angle) {
        0 -> GradientDrawable.Orientation.LEFT_RIGHT
        45 -> GradientDrawable.Orientation.BL_TR
        90 -> GradientDrawable.Orientation.BOTTOM_TOP
        135 -> GradientDrawable.Orientation.BR_TL
        180 -> GradientDrawable.Orientation.RIGHT_LEFT
        225 -> GradientDrawable.Orientation.TR_BL
        270 -> GradientDrawable.Orientation.TOP_BOTTOM
        315 -> GradientDrawable.Orientation.TL_BR
        else -> GradientDrawable.Orientation.LEFT_RIGHT
    }
}
