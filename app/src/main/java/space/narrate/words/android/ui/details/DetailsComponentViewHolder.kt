package space.narrate.words.android.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.wordset.Example
import space.narrate.words.android.data.repository.MerriamWebsterSource
import space.narrate.words.android.data.repository.WordPropertiesSource
import space.narrate.words.android.data.repository.WordsetSource
import space.narrate.words.android.util.toChip
import kotlinx.android.synthetic.main.details_component_examples.view.*
import kotlinx.android.synthetic.main.details_component_merriam_webster.view.*
import kotlinx.android.synthetic.main.details_component_title.view.*
import kotlinx.android.synthetic.main.details_component_wordset.view.*
import java.lang.RuntimeException

/**
 * A sealed class to hold all the [RecyclerView.ViewHolder]s which belong to each [DetailsComponent]
 * class. This makes it easy to abstract binding calls in [DetailsAdapter] by having a single
 * [bind] method which each subclass implements.
 */
sealed class DetailsComponentViewHolder(
        val view: View,
        val listener: DetailsComponentListener
): RecyclerView.ViewHolder(view) {

    companion object {
        /**
         * A static helper method to create the appropriate subclass of [DetailsComponentViewHolder]
         * given it's [type]
         */
        fun createViewHolder(
                parent: ViewGroup,
                type: Int,
                listener: DetailsComponentListener
        ) : DetailsComponentViewHolder {

            return when (type) {
                DetailsComponent.VIEW_TYPE_TITLE -> {
                    TitleComponentViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                    R.layout.details_component_title,
                                    parent,
                                    false
                            ), listener)
                }
                DetailsComponent.VIEW_TYPE_MERRIAM_WEBSTER -> {
                    MerriamWebsterComponentViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                    R.layout.details_component_merriam_webster,
                                    parent,
                                    false
                            ), listener)
                }
                DetailsComponent.VIEW_TYPE_WORDSET -> {
                    WordsetComponentViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                    R.layout.details_component_wordset,
                                    parent,
                                    false
                            ), listener)
                }
                DetailsComponent.VIEW_TYPE_EXAMPLE -> {
                    ExamplesComponentViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                    R.layout.details_component_examples,
                                    parent,
                                    false
                            ), listener)
                }
                else -> throw RuntimeException("Trying to inflate an invalid DetailsComponent type")
            }
        }
    }

    /**
     * This method to be called when [DetailsAdapter.onBindViewHolder] is called. Implementing
     * classes should expect [t] to be a subclass of [DetailsComponent] which corresponds with their
     * expected data. Set view data as you normally would in [RecyclerView.onBindViewHolder] here.
     */
    abstract fun bind(t: DetailsComponent)

    /**
     * The ViewHolder for [DetailsComponent.TitleComponent]. Handles setting data for the
     * [R.layout.details_component_title] layout.
     */
    class TitleComponentViewHolder(
            view: View,
            listener: DetailsComponentListener
    ): DetailsComponentViewHolder(view, listener) {
        override fun bind(t: DetailsComponent) {
            (t.source as? WordPropertiesSource)?.let {
                view.detailsComponentTitleText.text = it.word
            }
        }
    }

    /**
     * The ViewHolder for [DetailsComponent.MerriamWebsterComponent]. Handles setting data for
     * all Merriam-Webster data in the [R.layout.details_component_merriam_webster] layout as well
     * as passing UI events from its view to the given [DetailsComponentListener].
     */
    class MerriamWebsterComponentViewHolder(
            view: View,
            listener: DetailsComponentListener
    ): DetailsComponentViewHolder(view, listener),
            MerriamWebsterCardView.MerriamWebsterViewListener {

        init {
            view.detailsComponentMerriamWebsterCard.addListener(this)
        }

        override fun bind(t: DetailsComponent) {
            (t.source as? MerriamWebsterSource)?.let {
                view.detailsComponentMerriamWebsterCard.setSource(it.wordsDefinitions)
            }
        }

        override fun onRelatedWordClicked(word: String) {
            listener.onRelatedWordClicked(word)
        }

        override fun onSuggestionWordClicked(word: String) {
            listener.onSuggestionWordClicked(word)
        }

        override fun onAudioPlayClicked(url: String?) {
            listener.onAudioPlayClicked(url)
        }

        override fun onAudioStopClicked() {
            listener.onAudioStopClicked()
        }

        override fun onAudioClipError(message: String) {
            listener.onAudioClipError(message)
        }

        override fun onPermissionPaneDetailsClicked() {
            listener.onMerriamWebsterDetailsClicked()
        }

        override fun onPermissionPaneDismissClicked() {
            listener.onMerriamWebsterDismissClicked()
        }
    }

    /**
     * A ViewHolder for [DetailsComponent.WordsetComponent]. Handles setting all definitions
     * and synonyms for the [R.layout.details_component_wordset] layout as well as passing UI
     * events from its view to the given [DetailsComponentListener].
     */
    class WordsetComponentViewHolder(
            view: View,
            listener: DetailsComponentListener
    ): DetailsComponentViewHolder(view, listener) {


        override fun bind(t: DetailsComponent) {
            val source = t.source
            if (source is WordsetSource) {
                val meanings = source.wordAndMeaning.meanings

                //remove all views
                view.detailsComponentWordsetDefinitionsContainer?.removeAllViews()
                view.detailsComponentWordsetChipGroup?.removeAllViews()

                //add definition groups for each partOfSpeech
                meanings.groupBy { it.partOfSpeech }.entries.forEach { map ->
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
                meanings.map { it.synonyms }.flatten().forEach { synonym ->
                    view.detailsComponentWordsetChipGroup?.addView(
                            synonym.toChip(view.context!!, view.detailsComponentWordsetChipGroup) {
                                listener.onSynonymChipClicked(it.synonym)
                            }
                    )
                }
            }

        }

        private fun createPartOfSpeechView(pos: String): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
                    R.layout.details_part_of_speech_layout,
                    view?.detailsComponentWordsetDefinitionsContainer,
                    false
            ) as AppCompatTextView
            textView.text = pos
            return textView
        }

        private fun createDefinitionView(def: String): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
                    R.layout.details_definition_layout,
                    view?.detailsComponentWordsetDefinitionsContainer,
                    false
            ) as AppCompatTextView
            textView.text = ":$def"
            return textView
        }
    }

    /**
     * A ViewHolder for [DetailsComponent.ExamplesComponent]. Handles setting all WordSet examples
     * for the [R.layout.details_component_examples] layout
     */
    class ExamplesComponentViewHolder(
            view: View,
            listener: DetailsComponentListener
    ): DetailsComponentViewHolder(view, listener) {

        override fun bind(t: DetailsComponent) {
            val source = t.source
            if (source is WordsetSource) {
                val meanings = source.wordAndMeaning.meanings

                view.detailsComponentExamplesContainer?.removeAllViews()

                //add examples
                val examples = meanings.map { it.examples }
                if (examples.isNotEmpty()) {

                    // Loop to create and add each example
                    examples.flatten().forEach {
                        view.detailsComponentExamplesContainer?.addView(createExampleView(it))
                    }
                }
            }

        }

        private fun createExampleView(example: Example): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(
                    R.layout.details_example_layout,
                    view?.detailsComponentExamplesContainer,
                    false
            ) as AppCompatTextView
            textView.text = example.example
            return textView
        }

    }
}

