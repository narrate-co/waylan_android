package com.wordsdict.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.wordsdict.android.data.repository.*
import kotlin.reflect.KClass

class DetailsAdapter(private val listener: DetailsAdapter.Listener): ListAdapter<DetailsComponent, DetailsComponentViewHolder>(diffCallback), DetailsComponentListener {

    interface Listener {
        fun onRelatedWordClicked(relatedWord: String)
        fun onSuggestionWordClicked(suggestionWord: String)
        fun onSynonymChipClicked(synonym: String)
        fun onAudioClipError(message: String)
        fun onMerriamWebsterDismissClicked()
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DetailsComponent>() {
            override fun areItemsTheSame(oldItem: DetailsComponent, newItem: DetailsComponent): Boolean {
                return oldItem.equalTo(newItem)
            }
            override fun areContentsTheSame(oldItem: DetailsComponent, newItem: DetailsComponent): Boolean {
                return oldItem.contentsSameAs(newItem)
            }

            override fun getChangePayload(oldItem: DetailsComponent, newItem: DetailsComponent): Any? {
                return oldItem.getChangePayload(newItem)
            }
        }
    }

    inner class SourceHolder {

        private var wordId: String = ""

        private var propertiesSource: WordPropertiesSource? = null
        private var wordset: WordsetSource? = null
        private var merriamWebster: MerriamWebsterSource? = null
        private var firestoreUser: FirestoreUserSource? = null
        private var firestoreGlobal: FirestoreGlobalSource? = null

        fun addSource(source: WordSource) {

            println("SourceHolder::addSource - ${source::class.java.simpleName}")

            clearIfNewWordSource(source)

            when (source) {
                is WordPropertiesSource -> {
                    propertiesSource = source
                }
                is WordsetSource -> {
                    wordset = source
                }
                is MerriamWebsterSource -> {
                    println("SourceHolder::addMerriamWebsterSource. current = $merriamWebster, wordsAndDifinitions = ${source.wordsDefinitions}")

                    if (merriamWebster == null || source.wordsDefinitions.entries.map { it.definitions }.flatten().isNotEmpty() || source.wordsDefinitions.entries.map { it.word }.filterNotNull().map { it.suggestions }.flatten().isNotEmpty()) {
                        merriamWebster = source
                    }
                }
                is FirestoreUserSource -> {
                    firestoreUser = source
                }
                is FirestoreGlobalSource -> {
                    firestoreGlobal = source
                }
            }
        }

        fun removeSource(type: KClass<out WordSource>) {
            when (type) {
                WordPropertiesSource::class -> propertiesSource = null
                WordsetSource::class -> wordset = null
                MerriamWebsterSource::class -> merriamWebster = null
                FirestoreUserSource::class -> firestoreUser = null
                FirestoreGlobalSource::class -> firestoreGlobal = null
            }
        }

        fun getComponentsList(): List<DetailsComponent> {
            val list = mutableListOf<DetailsComponent>()
            propertiesSource?.let { list.add(DetailsComponent.TitleComponent(it)) }
            merriamWebster?.let { if (it.wordsDefinitions.entries.isNotEmpty()) list.add(DetailsComponent.MerriamWebsterComponent(it)) }
            wordset?.let { list.add(DetailsComponent.WordsetComponent(it)) }

            // Only add examples if there are any
            // TODO change this to always show if allowing users to add their own
            val examples = (wordset?.wordAndMeaning?.meanings?.map { it.examples } ?: emptyList()).flatten()
            if (examples.isNotEmpty()) {
                wordset?.let { list.add(DetailsComponent.ExamplesComponent(it)) }
            }
            return list
        }

        private fun clearIfNewWordSource(source: WordSource): Boolean {
            val newWordId: String? = when (source) {
                is WordsetSource -> source.wordAndMeaning.word?.word
                is MerriamWebsterSource -> source.wordsDefinitions?.entries.firstOrNull()?.word?.word
                is FirestoreUserSource -> source.userWord.word
                is FirestoreGlobalSource -> source.globalWord.word
                else -> return false
            }


            if (newWordId != null && newWordId.isNotBlank() && newWordId != wordId) {
                //new word coming in
                // clear source holder
                wordset = null
                merriamWebster = null
                firestoreUser = null
                firestoreGlobal = null

                wordId = newWordId
                return true
            } else {
                //same word. do nothing
                return false
            }
        }
    }

    private val sourceHolder = SourceHolder()

    fun submitWordSource(source: WordSource) {
        sourceHolder.addSource(source)
        submitList(sourceHolder.getComponentsList())
    }

    fun removeWordSource(type: KClass<out WordSource>) {
        sourceHolder.removeSource(type)
        submitList(sourceHolder.getComponentsList())
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.number

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsComponentViewHolder {
        return DetailsComponentViewHolder.createViewHolder(parent, viewType, this)
    }

    override fun onBindViewHolder(holder: DetailsComponentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onSynonymChipClicked(synonym: String) {
        listener.onSynonymChipClicked(synonym)
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        listener.onRelatedWordClicked(relatedWord)
    }

    override fun onSuggestionWordClicked(suggestionWord: String) {
        listener.onSuggestionWordClicked(suggestionWord)
    }

    override fun onAudioClipError(message: String) {
        listener.onAudioClipError(message)
    }

    override fun onMerriamWebsterDismissClicked() {
        listener.onMerriamWebsterDismissClicked()
    }
}