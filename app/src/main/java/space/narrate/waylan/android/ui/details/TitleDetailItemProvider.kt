package space.narrate.waylan.android.ui.details

import android.view.ViewGroup
import kotlinx.android.synthetic.main.details_title_item_layout.view.*
import space.narrate.waylan.android.R
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils

/**
 * An item provider which knows how to create a ViewHolder for the [DetailItemType.TITLE]
 * item type
 */
class TitleDetailItemProvider : DetailItemProvider {
    override val itemType: DetailItemType = DetailItemType.TITLE

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return TitleViewHolder(parent)
    }
}

class TitleViewHolder(
    parent: ViewGroup
): DetailItemViewHolder(
    AdapterUtils.inflate(parent, R.layout.details_title_item_layout)
) {
    override fun bind(item: DetailItemModel) {
        if (item !is TitleModel) return
        view.detailsComponentTitleText.text = item.word
    }
}
