package space.narrate.waylan.android.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.DetailsExamplesItemLayoutBinding
import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.core.util.visible
import space.narrate.waylan.core.R as coreR

/**
 * An item provider which knows how to create a ViewHolder for the [DetailItemType.EXAMPLE]
 * item type
 */
class WaylanExampleDetailItemProvider : DetailItemProvider {
    override val itemType: DetailItemType = DetailItemType.EXAMPLE

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return WaylanExampleViewHolder(
            DetailsExamplesItemLayoutBinding.inflate(parent.inflater, parent, false),
            listener
        )
    }
}

class WaylanExampleViewHolder(
    private val binding: DetailsExamplesItemLayoutBinding,
    val listener: DetailAdapterListener
): DetailItemViewHolder(
    binding.root
) {

    override fun bind(item: DetailItemModel) {
        if (item !is WaylanExampleModel) return
        binding.run {

            detailsComponentExamplesContainer.removeAllViews()

            //add examples
            val examples = item.examples
            if (examples.isNotEmpty()) {
                // Loop to create and add each example
                examples.forEach {
                    detailsComponentExamplesContainer.addView(createExampleView(it))
                }
                view.visible()
            } else {
                view.gone()
            }

        }
    }

    private fun createExampleView(example: Example): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(
            binding.detailsComponentExamplesContainer.context
        ).inflate(
            coreR.layout.details_example_layout,
            binding.detailsComponentExamplesContainer,
            false
        ) as AppCompatTextView
        textView.text = example.example
        return textView
    }

}
