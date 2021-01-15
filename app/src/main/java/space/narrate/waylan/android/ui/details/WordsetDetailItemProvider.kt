package space.narrate.waylan.android.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import space.narrate.waylan.android.R
import space.narrate.waylan.android.util.toChip
import space.narrate.waylan.android.databinding.DetailsWordsetItemLayoutBinding
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.inflater

/**
 * An item provider which knows how to create a ViewHolder for the [DetailItemType.WORDSET]
 * item type
 */
class WordsetDetailItemProvider : DetailItemProvider {
    override val itemType: DetailItemType = DetailItemType.WORDSET

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return WordsetViewHolder(
            DetailsWordsetItemLayoutBinding.inflate(parent.inflater, parent, false),
            listener
        )
    }
}

class WordsetViewHolder(
    private val binding: DetailsWordsetItemLayoutBinding,
    private val listener: DetailAdapterListener
): DetailItemViewHolder(binding.root) {

    override fun bind(item: DetailItemModel) {
        if (item !is WordsetModel) return
        //remove all views
        binding.run {
            detailsComponentWordsetDefinitionsContainer.removeAllViews()
            detailsComponentWordsetChipGroup.removeAllViews()

            //add definition groups for each partOfSpeech
            item.entry.meanings.groupBy { it.partOfSpeech }.entries.forEach { map ->
                // Create and add the overline part of speech titleRes view
                detailsComponentWordsetDefinitionsContainer.addView(
                    createPartOfSpeechView(map.key)
                )

                // Loop to create and add each definition
                map.value.forEach {
                    detailsComponentWordsetDefinitionsContainer.addView(
                        createDefinitionView(it.def)
                    )
                }
            }

            //add synonyms
            item.entry.meanings.map { it.synonyms }.flatten().forEach { synonym ->
                detailsComponentWordsetChipGroup.addView(
                    synonym.toChip(view.context, detailsComponentWordsetChipGroup) {
                        listener.onSynonymChipClicked(it.synonym)
                    }
                )
            }
        }
    }

    private fun createPartOfSpeechView(pos: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(
            binding.detailsComponentWordsetDefinitionsContainer.context
        ).inflate(
            R.layout.details_part_of_speech_layout,
            binding.detailsComponentWordsetDefinitionsContainer,
            false
        ) as AppCompatTextView
        textView.text = pos
        return textView
    }

    private fun createDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(
            binding.detailsComponentWordsetDefinitionsContainer.context
        ).inflate(
            R.layout.details_definition_layout,
            binding.detailsComponentWordsetDefinitionsContainer,
            false
        ) as AppCompatTextView
        textView.text = ":$def"
        return textView
    }
}
