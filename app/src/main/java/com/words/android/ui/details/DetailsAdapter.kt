package com.words.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.data.repository.WordSource

class DetailsAdapter(private val listener: DetailsAdapter.Listener): ListAdapter<DetailsComponent, DetailsComponentViewHolder>(diffCallback), DetailsComponentListener {

    interface Listener {
        fun onRelatedWordClicked(relatedWord: String)
        fun onSynonymChipClicked(synonym: String)
        fun onAudioClipError(message: String)
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

        private var wordset: WordSource.WordsetSource? = null
        private var merriamWebster: WordSource.MerriamWebsterSource? = null
        private var firestoreUser: WordSource.FirestoreUserSource? = null
        private var firestoreGlobal: WordSource.FirestoreGlobalSource? = null

        fun addSource(source: WordSource) {

            clearIfNewWordSource(source)

            when (source) {
                is WordSource.WordsetSource -> {
                    println("SourceHolder::adding wordset source ${source.wordAndMeaning}")
                    wordset = source
                }
                is WordSource.MerriamWebsterSource -> {
                    println("SourceHolder::adding mw source ${source.wordsAndDefs}")
                    merriamWebster = source
                }
                is WordSource.FirestoreUserSource -> {
                    println("SourceHolder::adding firestore user source ${source.userWord}")
                    firestoreUser = source
                }
                is WordSource.FirestoreGlobalSource -> {
                    println("SourceHolder::adding firestore global source ${source.globalWord}")
                    firestoreGlobal = source
                }
            }
        }

        fun getComponentsList(): List<DetailsComponent> {
            val list = mutableListOf<DetailsComponent>()
            wordset?.let { list.add(DetailsComponent.TitleComponent(it)) }
            merriamWebster?.let { if (it.wordsAndDefs.isNotEmpty()) list.add(DetailsComponent.MerriamWebsterComponent(it)) }
            wordset?.let { list.add(DetailsComponent.WordsetComponent(it)) }
            wordset?.let { list.add(DetailsComponent.ExamplesComponent(it)) }
            return list
        }

        private fun clearIfNewWordSource(source: WordSource): Boolean {
            val newWordId: String? = when (source) {
                is WordSource.WordsetSource -> source.wordAndMeaning.word?.word
                is WordSource.MerriamWebsterSource -> source.wordsAndDefs?.firstOrNull()?.word?.word
                is WordSource.FirestoreUserSource -> source.userWord.word
                is WordSource.FirestoreGlobalSource -> source.globalWord.word
                else -> return false
            }

            println("SourceHolder::clearIfNewWordSource - wordId = $wordId, newWordId = $newWordId")

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


    override fun getItemViewType(position: Int): Int = getItem(position).type.number

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsComponentViewHolder {
        return DetailsComponentViewHolder.createViewHolder(parent, viewType, this)
    }

    override fun onBindViewHolder(holder: DetailsComponentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //TODO override onBindViewHolder w/ payload

    override fun onSynonymChipClicked(synonym: String) {
        listener.onSynonymChipClicked(synonym)
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        listener.onRelatedWordClicked(relatedWord)
    }

    override fun onAudioClipError(message: String) {
        listener.onAudioClipError(message)
    }

}