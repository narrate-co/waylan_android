package space.narrate.waylan.settings.ui.addons

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.elevation.ElevationOverlayProvider
import space.narrate.waylan.settings.R
import space.narrate.waylan.settings.databinding.AddOnPreviewItemBinding

/**
 * A ViewHolder for [AddOnsPreviewAdapter] that controls a single preview of an add-on.
 */
class AddOnViewPreviewHolder(
    private val binding: AddOnPreviewItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    init {
        val surfaceColor = ElevationOverlayProvider(binding.card.context)
            .compositeOverlayWithThemeSurfaceColorIfNeeded(binding.card.elevation)
        val fadeGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.TRANSPARENT, surfaceColor)
        )
        binding.fadeView.background = fadeGradient
    }

    fun onBind(addOn: AddOnItemModel) {
        val layout = when (addOn) {
            is AddOnItemModel.MerriamWebster -> R.layout.add_on_merriam_webster_preview_content
            is AddOnItemModel.MerriamWebsterThesaurus -> R.layout.add_on_merriam_webster_thesaurus_preview_content
        }

        // Replace the card's content with a preview of this add-on's content.
        binding.cardContentContainer.removeAllViews()
        binding.cardContentContainer.addView(
            LayoutInflater.from(binding.cardContentContainer.context)
                .inflate(layout, binding.cardContentContainer, false)
        )
    }
}