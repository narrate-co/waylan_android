package com.words.android.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.R
import com.words.android.data.repository.WordSource

class WordsAdapter(private val handlers: WordAdapterHandlers): ListAdapter<WordSource, WordViewHolder>(diffCallback), WordViewHolder.WordViewHolderHandlers {

    companion object {
        val diffCallback: DiffUtil.ItemCallback<WordSource> = object : DiffUtil.ItemCallback<WordSource>() {
            override fun areItemsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                return oldItem::class.java.simpleName == newItem::class.java.simpleName
            }

            override fun areContentsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                if (oldItem is WordSource.SimpleWordSource && newItem is WordSource.SimpleWordSource) return areSimpleWordSourceContentsTheSame(oldItem, newItem)
                if (oldItem is WordSource.FirestoreUserSource && newItem is WordSource.FirestoreUserSource) return areFirestoreUserSourceContentsTheSame(oldItem, newItem)
                if (oldItem is WordSource.SuggestSource && newItem is WordSource.SuggestSource) return areSuggestSourceContentsTheSame(oldItem, newItem)
                return false
            }

            private fun areSimpleWordSourceContentsTheSame(oldItem: WordSource.SimpleWordSource, newItem: WordSource.SimpleWordSource): Boolean {
                return oldItem.word.popularity == newItem.word.popularity && oldItem.word.modified == newItem.word.modified
            }

            private fun areFirestoreUserSourceContentsTheSame(oldItem: WordSource.FirestoreUserSource, newItem: WordSource.FirestoreUserSource): Boolean {
                return oldItem.userWord.word == newItem.userWord.word
            }

            private fun areSuggestSourceContentsTheSame(oldItem: WordSource.SuggestSource, newItem: WordSource.SuggestSource): Boolean {
                return oldItem.item.term == newItem.item.term
                        && oldItem.item.distance == newItem.item.distance
                        && oldItem.item.count == newItem.item.count
            }
        }
    }

    interface WordAdapterHandlers {
        fun onWordClicked(word: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_word_layout, parent, false), this)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onWordClicked(word: String) {
        handlers.onWordClicked(word)
    }

}