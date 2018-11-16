package com.words.android.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.R
import com.words.android.data.repository.FirestoreUserSource
import com.words.android.data.repository.SimpleWordSource
import com.words.android.data.repository.SuggestSource
import com.words.android.data.repository.WordSource

class SearchAdapter(private val handlers: WordAdapterHandlers): ListAdapter<WordSource, SearchViewHolder>(diffCallback), SearchViewHolder.SearchViewHolderHandlers {

    companion object {
        val diffCallback: DiffUtil.ItemCallback<WordSource> = object : DiffUtil.ItemCallback<WordSource>() {
            override fun areItemsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                return oldItem::class.java.simpleName == newItem::class.java.simpleName
            }

            override fun areContentsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                if (oldItem is SimpleWordSource && newItem is SimpleWordSource) return areSimpleWordSourceContentsTheSame(oldItem, newItem)
                if (oldItem is FirestoreUserSource && newItem is FirestoreUserSource) return areFirestoreUserSourceContentsTheSame(oldItem, newItem)
                if (oldItem is SuggestSource && newItem is SuggestSource) return areSuggestSourceContentsTheSame(oldItem, newItem)
                return false
            }

            private fun areSimpleWordSourceContentsTheSame(oldItem: SimpleWordSource, newItem: SimpleWordSource): Boolean {
                return oldItem.word.popularity == newItem.word.popularity && oldItem.word.modified == newItem.word.modified
            }

            private fun areFirestoreUserSourceContentsTheSame(oldItem: FirestoreUserSource, newItem: FirestoreUserSource): Boolean {
                return oldItem.userWord.word == newItem.userWord.word
            }

            private fun areSuggestSourceContentsTheSame(oldItem: SuggestSource, newItem: SuggestSource): Boolean {
                return oldItem.item.term == newItem.item.term
                        && oldItem.item.distance == newItem.item.distance
                        && oldItem.item.count == newItem.item.count
            }
        }
    }

    interface WordAdapterHandlers {
        fun onWordClicked(word: WordSource)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_word_layout, parent, false), this)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onWordClicked(word: WordSource) {
        handlers.onWordClicked(word)
    }
}