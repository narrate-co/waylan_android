package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.chip.Chip
import space.narrate.waylan.core.R
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.remainingDays
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.util.gone

/**
 * Wrapper around Chip to be used as a text label.
 *
 * A text label is a pill with a reduced height to minimize interactability and instead
 * provide a visual for exposing information about content. This class helps encapsulate usage
 * of text labels across Waylan by exposing extension methods for a limited number of "use cases"
 * such as using a text label for showing the state of an Add-on.
 */
class TextLabelChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.styleTextLabel
) : Chip(context, attrs, defStyleAttr) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        layoutParams = layoutParams.apply {
            height = context.resources.getDimensionPixelSize(R.dimen.text_label_height)
        }
    }
}

fun TextLabelChip.configureWithUserAddOn(userAddOn: UserAddOn?) {
    if (userAddOn == null) {
        gone()
        return
    }

    val textString = when (userAddOn.state) {
        AddOnState.FREE_TRIAL_VALID -> {
            resources.getString(
                R.string.add_on_text_label_free_trial_days_remaining,
                userAddOn.remainingDays.toString()
            )
        }
        AddOnState.FREE_TRIAL_EXPIRED -> {
            resources.getString(R.string.add_on_text_label_free_trial_expired)
        }
        AddOnState.PURCHASED_VALID ->
            resources.getString(R.string.add_on_text_label_plugin_expired)
        AddOnState.PURCHASED_EXPIRED ->
            resources.getString(
                R.string.add_on_text_label_renew_days_remaining,
                userAddOn.remainingDays.toString()
            )
        else -> ""
    }

    val newVisibility = when (userAddOn.state) {
        AddOnState.FREE_TRIAL_VALID,
        AddOnState.FREE_TRIAL_EXPIRED,
        AddOnState.PURCHASED_EXPIRED -> View.VISIBLE
        else -> View.GONE
    }

    text = textString
    visibility = newVisibility
}