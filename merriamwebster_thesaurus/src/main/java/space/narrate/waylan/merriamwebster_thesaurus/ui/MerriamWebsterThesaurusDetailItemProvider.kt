package space.narrate.waylan.merriamwebster_thesaurus.ui

import android.view.ViewGroup
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.merriamwebster_thesaurus.R
import space.narrate.waylan.merriamwebster_thesaurus.databinding.MwThesaurusItemLayoutBinding

class MerriamWebsterThesaurusDetailItemProvider : DetailItemProvider {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER_THESAURUS

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return MerriamWebsterThesaurusViewHolder(
            MwThesaurusItemLayoutBinding.inflate(parent.inflater),
            listener
        )
    }
}

class MerriamWebsterThesaurusViewHolder(
    private val binding: MwThesaurusItemLayoutBinding,
    private val listener: DetailAdapterListener
) : DetailItemViewHolder(
    binding.root
) {
    override fun bind(item: DetailItemModel) {
        if (item !is MerriamWebsterThesaurusModel) return
        // TODO: bind stuff
    }
}