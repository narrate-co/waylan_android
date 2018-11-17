package com.wordsdict.android.ui.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.data.disk.wordset.Synonym
import com.wordsdict.android.data.repository.FirestoreGlobalSource
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.util.toChip
import kotlinx.android.synthetic.main.list_item_layout.view.*
import org.threeten.bp.OffsetDateTime

class ListTypeViewHolder(private val view: View, private val listener: ListTypeViewHolderListener): RecyclerView.ViewHolder(view) {

    interface ListTypeViewHolderListener {
        fun onWordClicked(word: String)
    }

    fun bind(source: WordSource) {
        //Set word
        when (source) {
            is FirestoreUserSource -> bindFirestoreUserSource(source)
            is FirestoreGlobalSource -> bindFirestoreGlobalSource(source)
        }


    }
    private fun bindFirestoreUserSource(source: FirestoreUserSource) {
        bindSource(
                source.userWord.word,
                source.userWord.partOfSpeechPreview,
                source.userWord.defPreview,
                source.userWord.synonymPreview

        )
    }

    private fun bindFirestoreGlobalSource(source: FirestoreGlobalSource) {
        bindSource(
                source.globalWord.word,
                source.globalWord.partOfSpeechPreview,
                source.globalWord.defPreview,
                source.globalWord.synonymPreview
        )
    }

    private fun bindSource(word: String, partOfSpeechPreview: MutableMap<String, String>, defPreview: MutableMap<String, String>, synonymPreview: MutableMap<String, String>) {

        view.word.text = word

        //Set part of speech
        view.partOfSpeech.text = partOfSpeechPreview.keys.first()

        //Set definition
        defPreview.map { it.key }.firstOrNull()?.let {
            view.definition.text = it
        }

        //Set synonym chips
        view.chipGroup.removeAllViews()
        synonymPreview.forEach {
            view.chipGroup.addView(Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now()).toChip(view.context, view.chipGroup) {
                listener.onWordClicked(it.synonym)
            })
        }

        view.itemContainer.setOnClickListener {
            listener.onWordClicked(word)
        }
    }

}

