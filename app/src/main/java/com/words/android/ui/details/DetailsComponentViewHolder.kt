package com.words.android.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.words.android.R
import com.words.android.data.disk.wordset.Example
import com.words.android.data.disk.wordset.Meaning
import com.words.android.data.repository.MerriamWebsterSource
import com.words.android.data.repository.WordPropertiesSource
import com.words.android.data.repository.WordSource
import com.words.android.data.repository.WordsetSource
import com.words.android.util.Bindable
import com.words.android.util.toChip
import kotlinx.android.synthetic.main.details_component_examples.view.*
import kotlinx.android.synthetic.main.details_component_merriam_webster.view.*
import kotlinx.android.synthetic.main.details_component_title.view.*
import kotlinx.android.synthetic.main.details_component_wordset.view.*
import java.lang.RuntimeException

sealed class DetailsComponentViewHolder(val view: View, val listener: DetailsComponentListener): RecyclerView.ViewHolder(view), Bindable<DetailsComponent> {

    companion object {
        fun createViewHolder(parent: ViewGroup, type: Int, listener: DetailsComponentListener): DetailsComponentViewHolder {
            return when (type) {
                DetailsComponent.Type.TITLE.number -> TitleComponentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.details_component_title, parent, false), listener)
                DetailsComponent.Type.MERRIAM_WEBSTER.number -> MerriamWebsterComponentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.details_component_merriam_webster, parent, false), listener)
                DetailsComponent.Type.WORDSET.number -> WordsetComponentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.details_component_wordset, parent, false), listener)
                DetailsComponent.Type.EXAMPLE.number -> ExamplesComponentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.details_component_examples, parent, false), listener)
                else -> throw RuntimeException("Trying to inflate an invalid viewType for a DetailsComponent")
            }
        }
    }


    class TitleComponentViewHolder(view: View, listener: DetailsComponentListener): DetailsComponentViewHolder(view, listener) {
        override fun bind(t: DetailsComponent) {
            (t.source as? WordPropertiesSource)?.let {
                view.detailsComponentTitleText.text = it.props.word
            }
        }
    }

    class MerriamWebsterComponentViewHolder(view: View, listener: DetailsComponentListener): DetailsComponentViewHolder(view, listener), MerriamWebsterCard.MerriamWebsterViewListener {

        init {
            view.detailsComponentMerriamWebsterCard.addListener(this)
        }

        override fun bind(t: DetailsComponent) {
            (t.source as? MerriamWebsterSource)?.let {
                view.detailsComponentMerriamWebsterCard.setWordAndDefinitions(it.wordsDefinitions)
            }
        }

        override fun onRelatedWordClicked(word: String) {
            listener.onRelatedWordClicked(word)
        }

        override fun onAudioClipError(message: String) {
            listener.onAudioClipError(message)
        }

        override fun onDismissCardClicked() {
            listener.onMerriamWebsterDismissClicked()
        }
    }

    class WordsetComponentViewHolder(view: View, listener: DetailsComponentListener): DetailsComponentViewHolder(view, listener) {

        private var currentMeanings: List<Meaning> = emptyList()

        override fun bind(t: DetailsComponent) {
            (t.source as? WordsetSource)?.let {
                val meanings = it.wordAndMeaning.meanings
                if (currentMeanings.containsAll(meanings)) return

                currentMeanings = meanings

                //remove all views
                view.detailsComponentWordsetDefinitionsContainer?.removeAllViews()
                view.detailsComponentWordsetChipGroup?.removeAllViews()

                //add definition groups for each partOfSpeech
                meanings.groupBy { it.partOfSpeech }.entries.forEach {
                    view.detailsComponentWordsetDefinitionsContainer?.addView(createPartOfSpeechView(it.key))
                    it.value.forEach {
                        view.detailsComponentWordsetDefinitionsContainer?.addView(createDefinitionView(it.def))
                    }
                }

                //add synonyms
                meanings.map { it.synonyms }.flatten().forEach {
                    view.detailsComponentWordsetChipGroup?.addView(it.toChip(view.context!!, view.detailsComponentWordsetChipGroup) {
                        listener.onSynonymChipClicked(it.synonym)
                    })
                }
            }




        }

        private fun createPartOfSpeechView(pos: String): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(R.layout.details_part_of_speech_layout, view?.detailsComponentWordsetDefinitionsContainer, false) as AppCompatTextView
            textView.text = pos
            return textView
        }

        private fun createDefinitionView(def: String): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(R.layout.details_definition_layout, view?.detailsComponentWordsetDefinitionsContainer, false) as AppCompatTextView
            textView.text = ":$def"
            return textView
        }
    }

    class ExamplesComponentViewHolder(view: View, listener: DetailsComponentListener): DetailsComponentViewHolder(view, listener) {

        private var currentMeanings: List<Meaning> = emptyList()

        override fun bind(t: DetailsComponent) {
            (t.source as? WordsetSource)?.let {
                val meanings = it.wordAndMeaning.meanings
                if (currentMeanings.containsAll(meanings)) return

                currentMeanings = meanings


                view.detailsComponentExamplesContainer?.removeAllViews()

                //add examples
                val examples = meanings.map { it.examples }
                if (examples.isNotEmpty()) {
                    examples.flatten().forEach {
                        view.detailsComponentExamplesContainer?.addView(createExampleView(it))
                    }
                }
            }

        }


        private fun createExampleView(example: Example): AppCompatTextView {
            val textView: AppCompatTextView = LayoutInflater.from(view.context).inflate(R.layout.details_example_layout, view?.detailsComponentExamplesContainer, false) as AppCompatTextView
            textView.text = example.example
            return textView
        }

    }
}

