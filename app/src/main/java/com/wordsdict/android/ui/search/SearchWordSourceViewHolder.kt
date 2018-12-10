package com.wordsdict.android.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.R
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.ui.common.HeaderBanner
import com.wordsdict.android.ui.common.HeaderBannerBinder
import com.wordsdict.android.ui.common.HeaderBannerListener
import com.wordsdict.android.util.getColorFromAttr
import kotlinx.android.synthetic.main.list_banner_layout.view.*
import kotlinx.android.synthetic.main.search_word_layout.view.*

/**
 * A sealed class to be used to generically define the type of [RecyclerView.ViewHolder] used
 * in [SearchAdapter]
 */
sealed class SearchViewHolder(val view: View): RecyclerView.ViewHolder(view)


/**
 * A [SearchViewHolder] that handles binding a [SimpleWordSource], [FirestoreUserSource]
 * or [SuggestSource] to [R.layout.search_word_layout].
 *
 */
class SearchWordSourceViewHolder(view: View, private val handlers: SearchViewHolderHandlers):
        SearchViewHolder(view) {

    interface SearchViewHolderHandlers {
        fun onWordClicked(word: WordSource)
    }

    /**
     * Bind a [SimpleWordSource], [FirestoreUserSource] or [SuggestSource] to this ViewHolder's
     * view.
     *
     * A [SimpleWordSource] is comes from the local WordSet db and uses a search icon, a
     * [FirestoreUserSource] is considered a recently or previously viewed word and uses
     * a recent icon and a [SuggestSource] is a spelling correction and will use a smart icon.
     */
    fun bind(word: WordSource) {
        when (word) {
            is SimpleWordSource -> {
                bindSource(word.word.word, R.drawable.ic_round_search_outlined_24px, word)
            }
            is FirestoreUserSource -> {
                bindSource(word.userWord.word, R.drawable.ic_round_recent_outlined_24px, word)
            }
            is SuggestSource -> {
                bindSource(word.item.term, R.drawable.ic_round_smart_outlined_24px, word)
            }
            else -> clear()
        }
    }

    private fun bindSource(term: String, imgRes: Int, word: WordSource) {
        view.wordText.text = term
        view.wordIcon.setImageResource(imgRes)
        view.container.setOnClickListener { handlers.onWordClicked(word) }
    }

    fun clear() {
        view.wordText.text = ""
        view.wordIcon.setImageDrawable(null)
    }
}

/**
 * A [SearchViewHolder] that handles binding a [HeaderBanner] with [HeaderBannerBinder]
 */
class SearchHeaderViewHolder(
        view: View,
        private val listener: HeaderBannerListener
): SearchViewHolder(view), HeaderBannerBinder {

    fun bind(banner: HeaderBanner?) {
        view.banner.setBackgroundColor(view.context.getColorFromAttr(R.attr.surfaceBColor))
        bindHeaderBanner(view.banner, banner, listener)
    }
}



