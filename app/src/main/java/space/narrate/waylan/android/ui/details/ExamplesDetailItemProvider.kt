package space.narrate.waylan.android.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.details_examples_item_layout.view.*
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.visible

/**
 * An item provider which knows how to create a ViewHolder for the [DetailItemType.EXAMPLE]
 * item type
 */
class ExamplesDetailItemProvider : DetailItemProvider {
    override val itemType: DetailItemType = DetailItemType.EXAMPLE

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return ExamplesViewHolder(
            parent,
            listener
        )
    }
}

class ExamplesViewHolder(
    parent: ViewGroup,
    val listener: DetailAdapterListener
): DetailItemViewHolder(
    AdapterUtils.inflate(parent, R.layout.details_examples_item_layout)
) {

    override fun bind(item: DetailItemModel) {
        if (item !is ExamplesModel) return
        view.detailsComponentExamplesContainer?.removeAllViews()

        //add examples
        val examples = item.examples
        if (examples.isNotEmpty()) {
            // Loop to create and add each example
            examples.forEach {
                view.detailsComponentExamplesContainer?.addView(createExampleView(it))
            }
            view.visible()
        } else {
            view.gone()
        }

    }

    private fun createExampleView(example: Example): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
            R.layout.details_example_layout,
            view.detailsComponentExamplesContainer,
            false
        ) as AppCompatTextView
        textView.text = example.example
        return textView
    }

}
