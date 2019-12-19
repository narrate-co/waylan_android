package space.narrate.waylan.merriamwebster.ui

import android.view.ViewGroup
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.merriamwebster.R

/**
 * A [DetailItemProvider] which knows how to create a ViewHolder for the
 * [DetailItemType.MERRIAM_WEBSTER] item type
 */
class MerriamWebsterDetailItemProvider : DetailItemProvider {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return MerriamWebsterViewHolder(
            parent,
            listener
        )
    }
}

class MerriamWebsterViewHolder(
    parent: ViewGroup,
    listener: DetailAdapterListener
): DetailItemViewHolder(
    AdapterUtils.inflate(parent, R.layout.merriam_webster_item_layout)
) {

    private val merriamWebsterCard: MerriamWebsterCardView =
        view.findViewById(R.id.detailsComponentMerriamWebsterCard)

    init {
        merriamWebsterCard.setListener(listener)
    }

    override fun bind(item: DetailItemModel) {
        if (item !is MerriamWebsterModel) return
        merriamWebsterCard.setSource(item.entries, item.userAddOn)
    }
}
