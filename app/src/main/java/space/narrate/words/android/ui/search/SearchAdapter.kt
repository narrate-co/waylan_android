package space.narrate.words.android.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import space.narrate.words.android.R
import space.narrate.words.android.data.repository.FirestoreUserSource
import space.narrate.words.android.data.repository.SimpleWordSource
import space.narrate.words.android.data.repository.SuggestSource
import space.narrate.words.android.data.repository.WordSource
import space.narrate.words.android.ui.common.HeaderListAdapter
import space.narrate.words.android.ui.common.HeaderBanner
import space.narrate.words.android.ui.common.HeaderBannerListener
import java.lang.IllegalStateException

/**
 * A [RecyclerView.Adapter] which displays a simple list of [WordSource] items. This
 * adapter is able to display [SimpleWordSource], [FirestoreUserSource] and [SuggestSource] items
 * as well as an optional [HeaderBanner].
 *
 * A [SimpleWordSource] represents an item that partially matches a user search query
 * A [FirestoreUserSource] represents an item that has been recently viewed by the user
 * A [SuggestSource] represents possible correct spellings for a user search query
 */
class SearchAdapter(
        private val handlers: WordAdapterHandlers
): HeaderListAdapter<WordSource, SearchViewHolder, HeaderBanner>(diffCallback),
        SearchWordSourceViewHolder.SearchViewHolderHandlers,
        HeaderBannerListener {

    interface WordAdapterHandlers: HeaderBannerListener {
        /**
         * Called when an item is clicked, passing that item's [word] as it appears in
         * the dictionary
         */
        fun onWordClicked(word: WordSource)
    }

    companion object {
        // A DiffUtil.ItemCallback to be used by the underlying ListAdapter when
        // determining when an item has been added, updated or removed
        val diffCallback = object : DiffUtil.ItemCallback<WordSource>() {
            override fun areItemsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                return oldItem::class.java.simpleName == newItem::class.java.simpleName
            }

            override fun areContentsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                if (oldItem is SimpleWordSource && newItem is SimpleWordSource) {
                    return areSimpleWordSourceContentsTheSame(oldItem, newItem)
                }
                if (oldItem is FirestoreUserSource && newItem is FirestoreUserSource) {
                    return areFirestoreUserSourceContentsTheSame(oldItem, newItem)
                }
                if (oldItem is SuggestSource && newItem is SuggestSource) {
                    return areSuggestSourceContentsTheSame(oldItem, newItem)
                }
                return false
            }

            private fun areSimpleWordSourceContentsTheSame(
                    oldItem: SimpleWordSource,
                    newItem: SimpleWordSource
            ): Boolean {
                return oldItem.word.popularity == newItem.word.popularity
                        && oldItem.word.modified == newItem.word.modified
            }

            private fun areFirestoreUserSourceContentsTheSame(
                    oldItem: FirestoreUserSource,
                    newItem: FirestoreUserSource
            ): Boolean {
                return oldItem.userWord.word == newItem.userWord.word
            }

            private fun areSuggestSourceContentsTheSame(
                    oldItem: SuggestSource,
                    newItem: SuggestSource
            ): Boolean {
                return oldItem.item.term == newItem.item.term
                        && oldItem.item.distance == newItem.item.distance
                        && oldItem.item.count == newItem.item.count
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                SearchHeaderViewHolder(
                        LayoutInflater.from(parent.context).inflate(
                                R.layout.list_banner_layout,
                                parent,
                                false
                        ), this)
            }
            VIEW_TYPE_ITEM -> {
                SearchWordSourceViewHolder(LayoutInflater.from(parent.context).inflate(
                        R.layout.search_word_layout,
                        parent,
                        false
                ), this)
            }
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

    override fun onBannerClicked(banner: HeaderBanner) {
        handlers.onBannerClicked(banner)
    }

    override fun onBannerTopButtonClicked(banner: HeaderBanner) {
        handlers.onBannerTopButtonClicked(banner)
    }

    override fun onBannerBottomButtonClicked(banner: HeaderBanner) {
        handlers.onBannerBottomButtonClicked(banner)
    }
}