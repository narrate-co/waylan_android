package space.narrate.words.android.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.details_component_examples.view.*
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.wordset.Example
import kotlinx.android.synthetic.main.details_component_merriam_webster.view.*
import kotlinx.android.synthetic.main.details_component_title.view.*
import kotlinx.android.synthetic.main.details_component_wordset.view.*
import space.narrate.words.android.util.AdapterUtils
import space.narrate.words.android.util.gone
import space.narrate.words.android.util.toChip
import space.narrate.words.android.util.visible

sealed class DetailItemViewHolder<T : DetailItemModel>(
    val view: View
): RecyclerView.ViewHolder(view) {

    abstract fun bind(item: T)

    class TitleViewHolder(
        parent: ViewGroup
    ): DetailItemViewHolder<DetailItemModel.TitleModel>(
        AdapterUtils.inflate(parent, R.layout.details_component_title)
    ) {
        override fun bind(item: DetailItemModel.TitleModel) {
            view.detailsComponentTitleText.text = item.word
        }
    }

    class MerriamWebsterViewHolder(
        parent: ViewGroup,
        listener: DetailItemAdapter.Listener
    ): DetailItemViewHolder<DetailItemModel.MerriamWebsterModel>(
        AdapterUtils.inflate(parent, R.layout.details_component_merriam_webster)
    ) {

        private val merriamWebsterCard: MerriamWebsterCardView =
            view.findViewById(R.id.detailsComponentMerriamWebsterCard)

        init {
            view.detailsComponentMerriamWebsterCard.setListener(listener)
        }

        override fun bind(item: DetailItemModel.MerriamWebsterModel) {
            merriamWebsterCard.setSource(item.entries, item.user)
        }
    }

    class WordsetViewHolder(
        parent: ViewGroup,
        private val listener: DetailItemAdapter.Listener
    ): DetailItemViewHolder<DetailItemModel.WordsetModel>(
        AdapterUtils.inflate(parent, R.layout.details_component_wordset)
    ) {

        override fun bind(item: DetailItemModel.WordsetModel) {
            //remove all views
            view.detailsComponentWordsetDefinitionsContainer?.removeAllViews()
            view.detailsComponentWordsetChipGroup?.removeAllViews()

            //add definition groups for each partOfSpeech
            item.entry.meanings.groupBy { it.partOfSpeech }.entries.forEach { map ->
                // Create and add the overline part of speech title view
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

    class ExamplesViewHolder(
        parent: ViewGroup,
        val listener: DetailItemAdapter.Listener
    ): DetailItemViewHolder<DetailItemModel.ExamplesModel>(
        AdapterUtils.inflate(parent, R.layout.details_component_examples)
    ) {

        override fun bind(item: DetailItemModel.ExamplesModel) {
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
}

