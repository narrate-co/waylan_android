package space.narrate.waylan.android.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.details_wordset_item_layout.view.*
import space.narrate.waylan.android.R
import space.narrate.waylan.android.util.toChip
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils

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
        return WordsetViewHolder(parent, listener)
    }
}

class WordsetViewHolder(
    parent: ViewGroup,
    private val listener: DetailAdapterListener
): DetailItemViewHolder(
    AdapterUtils.inflate(parent, R.layout.details_wordset_item_layout)
) {

    override fun bind(item: DetailItemModel) {
        if (item !is WordsetModel) return
        //remove all views
        view.detailsComponentWordsetDefinitionsContainer?.removeAllViews()
        view.detailsComponentWordsetChipGroup?.removeAllViews()

        //add definition groups for each partOfSpeech
        item.entry.meanings.groupBy { it.partOfSpeech }.entries.forEach { map ->
            // Create and add the overline part of speech titleRes view
            view.detailsComponentWordsetDefinitionsContainer?.addView(
                createPartOfSpeechView(map.key)
            )

            // Loop to create and add each definition
            map.value.forEach {
                view.detailsComponentWordsetDefinitionsContainer?.addView(
                    createDefinitionView(it.def)
                )
            }
        }

        //add synonyms
        item.entry.meanings.map { it.synonyms }.flatten().forEach { synonym ->
            view.detailsComponentWordsetChipGroup?.addView(
                synonym.toChip(view.context, view.detailsComponentWordsetChipGroup) {
                    listener.onSynonymChipClicked(it.synonym)
                }
            )
        }

    }

    private fun createPartOfSpeechView(pos: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
            R.layout.details_part_of_speech_layout,
            view.detailsComponentWordsetDefinitionsContainer,
            false
        ) as AppCompatTextView
        textView.text = pos
        return textView
    }

    private fun createDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
            R.layout.details_definition_layout,
            view.detailsComponentWordsetDefinitionsContainer,
            false
        ) as AppCompatTextView
        textView.text = ":$def"
        return textView
    }
}
