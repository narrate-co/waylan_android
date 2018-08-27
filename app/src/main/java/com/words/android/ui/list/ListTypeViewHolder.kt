package com.words.android.ui.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.words.android.data.disk.wordset.Synonym
import com.words.android.data.repository.Word
import com.words.android.util.toChip
import kotlinx.android.synthetic.main.list_item_layout.view.*
import org.threeten.bp.OffsetDateTime

class ListTypeViewHolder(private val view: View, private val listener: ListTypeViewHolderListener): RecyclerView.ViewHolder(view) {

    interface ListTypeViewHolderListener {
        fun onWordClicked(word: String)
    }

    fun bind(word: Word) {
        //Set word
        word.userWord?.let {
            view.word.text = it.word
        }

        //Set part of speech
        view.partOfSpeech.text = word.userWord?.partOfSpeechPreview?.keys?.first()

        //Set definition
        word.userWord?.defPreview?.map { it.key }?.firstOrNull()?.let {
            view.definition.text = it
        }

        //Set synonym chips
        view.chipGroup.removeAllViews()
        word.userWord?.synonymPreview?.forEach {
            view.chipGroup.addView(Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now()).toChip(view.context, view.chipGroup) {
                //TODO set chip listener
                listener.onWordClicked(it.synonym)
            })
        }

        view.itemContainer.setOnClickListener {
            word.userWord?.word?.let { listener.onWordClicked(it) }
        }

    }
}

