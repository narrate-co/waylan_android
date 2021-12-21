package space.narrate.waylan.core.util

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.annotation.DimenRes
import com.google.android.material.elevation.SurfaceColors

/**
 * Set the Activity's window background to a color drawable that is given the color of colorSurface
 * plus any overlay created for the given [elevation].
 */
fun Activity.windowTintElevation(@DimenRes elevation: Int) {
  val e = resources.getDimension(elevation)
  val color = SurfaceColors.getColorForElevation(this, e)
  window.setBackgroundDrawable(ColorDrawable(color))
}

