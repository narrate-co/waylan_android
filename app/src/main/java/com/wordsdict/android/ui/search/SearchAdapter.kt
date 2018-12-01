package com.wordsdict.android.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wordsdict.android.R
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.ui.common.HeaderListAdapter
import com.wordsdict.android.ui.list.Banner
import com.wordsdict.android.ui.list.BannerViewHolderListener
import java.lang.IllegalStateException

class SearchAdapter(
        private val handlers: WordAdapterHandlers
): HeaderListAdapter<WordSource, SearchViewHolder, Banner>(diffCallback), SearchWordSourceViewHolder.SearchViewHolderHandlers, BannerViewHolderListener {

    interface WordAdapterHandlers: BannerViewHolderListener {
        fun onWordClicked(word: WordSource)
    }

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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> SearchHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_banner_layout, parent, false), this)
            VIEW_TYPE_ITEM -> SearchWordSourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_word_layout, parent, false), this)
            else -> throw IllegalStateException("Unsupported viewType attempting to be inflated")
        }
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        when (holder) {
            is SearchHeaderViewHolder -> holder.bind(getHeader())
            is SearchWordSourceViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun onWordClicked(word: WordSource) {
        handlers.onWordClicked(word)
    }

    override fun onBannerClicked(banner: Banner) {
        handlers.onBannerClicked(banner)
    }

    override fun onBannerTopButtonClicked(banner: Banner) {
        handlers.onBannerTopButtonClicked(banner)
    }

    override fun onBannerBottomButtonClicked(banner: Banner) {
        handlers.onBannerBottomButtonClicked(banner)
    }
}