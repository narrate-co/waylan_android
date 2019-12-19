package space.narrate.waylan.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.snackbar.Snackbar
import space.narrate.waylan.core.R
import space.narrate.waylan.core.ui.common.SnackbarModel

/**
 * Possible ways in which a Words [Snackbar] can be styled
 */
private enum class SnackbarType {
    INFORMATIVE, ERROR
}

/**
 * Helper method to style a [Snackbar] which contains content which is informative, descriptive
 * or accessory in nature.
 *
 * Call this method after [Snackbar.make] and before [Snackbar.show]
 *
 * @param abovePeekedSheet true if this sheet will be displayed on a screen that contains
 *  the [SearchFragment] and should be given additional vertical offset to show above the peeked
 *  bottom search sheet.
 */
fun Snackbar.configInformative(context: Context): Snackbar {
    return config(SnackbarType.INFORMATIVE, context)
}

/**
 * Helper method to style a [Snackbar] which contains content about an error or invalid state.
 *
 * Call this method after [Snackbar.make] and before [Snackbar.show]
 *
 * @param abovePeekedSheet true if this sheet will be displayed on a screen that contains
 *  the [SearchFragment] and should be given additional vertical offset to show above the peeked
 *  bottom search sheet.
 */
fun Snackbar.configError(context: Context): Snackbar {
    return config(SnackbarType.ERROR, context)
}

/**
 * Given a Snackbar, alter it's background, message textRes appearance, button textRes appearance and
 * margins to create a floating card with rounded corners.
 *
 * TODO: Remove [abovePeekedSheet] and use anchorView instead.
 */
@SuppressLint("ResourceType")
private fun Snackbar.config(
    type: SnackbarType,
    context: Context
): Snackbar {
    val bgColorAttr = when (type) {
        SnackbarType.ERROR -> R.attr.colorError
        SnackbarType.INFORMATIVE -> R.attr.colorSurface
    }

    val background = AppCompatResources.getDrawable(
        context,
        R.drawable.snackbar_background
    )!!
    val elevationOverlayProvider = ElevationOverlayProvider(context)
    val backgroundTint = elevationOverlayProvider.compositeOverlayIfNeeded(
        context.getColorFromAttr(bgColorAttr),
        context.resources.getDimension(R.dimen.plane_03)
    )
    DrawableCompat.setTint(background, backgroundTint)
    val textAppearance = R.style.TextAppearance_Waylan_Body1
    val buttonTextAppearance = R.style.TextAppearance_Waylan_Button
    val textColor = when (type) {
        SnackbarType.ERROR -> context.getColorFromAttr(R.attr.colorOnError)
        SnackbarType.INFORMATIVE -> context.getColorFromAttr(R.attr.colorOnSurface)
    }

    view.background = background
    ViewCompat.setElevation(view, 6F)

    //alter textRes
    val tv = view.findViewById<TextView>(R.id.snackbar_text)
    TextViewCompat.setTextAppearance(tv, textAppearance)
    tv.setTextColor(textColor)
    // TODO: Use fontFamily theme attributes
    tv.typeface = ResourcesCompat.getFont(context, R.font.ibm_plex_serif_regular)

    //alter action
    val action = view.findViewById<TextView>(R.id.snackbar_action)
    TextViewCompat.setTextAppearance(action, buttonTextAppearance)
    action.setTextColor(textColor)
    // TODO: Use fontFamily theme attributes
    action.typeface = ResourcesCompat.getFont(context, R.font.ibm_plex_mono_medium)
    action.isAllCaps = false

    return this
}


fun SnackbarModel.make(view: View): Snackbar {
    val snackbar = Snackbar.make(
        view,
        textRes,
        when (length) {
            SnackbarModel.LENGTH_INDEFINITE -> Snackbar.LENGTH_INDEFINITE
            SnackbarModel.LENGTH_LONG -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
    )

    if (isError) {
        snackbar.configError(view.context)
    } else {
        snackbar.configInformative(view.context)
    }

    return snackbar
}
