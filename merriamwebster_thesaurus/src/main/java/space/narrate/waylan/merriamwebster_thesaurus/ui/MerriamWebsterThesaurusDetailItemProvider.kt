package space.narrate.waylan.merriamwebster_thesaurus.ui

import android.view.ViewGroup
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.merriamwebster_thesaurus.databinding.MwThesaurusItemLayoutBinding

/**
 * A [DetailItemProvider] which knows how to create a ViewHolder for the
 * [DetailItemType.MERRIAM_WEBSTER_THESAURUS] item type
 */
class MerriamWebsterThesaurusDetailItemProvider : DetailItemProvider {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER_THESAURUS

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return MerriamWebsterThesaurusViewHolder(
            MwThesaurusItemLayoutBinding.inflate(parent.inflater, parent, false),
            listener
        )
    }
}

class MerriamWebsterThesaurusViewHolder(
    private val binding: MwThesaurusItemLayoutBinding,
    listener: DetailAdapterListener
) : DetailItemViewHolder(
    binding.root
) {

    init {
        binding.detailsComponentMerriamWebsterThesaurusCard.setListener(listener)
    }

    override fun bind(item: DetailItemModel) {
        if (item !is MerriamWebsterThesaurusModel) return
        binding.detailsComponentMerriamWebsterThesaurusCard.setSource(item.entries, item.user)
    }
}