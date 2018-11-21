package com.wordsdict.android.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.R
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.util.Banner
import com.wordsdict.android.util.BannerViewHolder
import com.wordsdict.android.util.BannerViewHolderListener
import com.wordsdict.android.util.getColorFromAttr
import kotlinx.android.synthetic.main.list_banner_layout.view.*
import kotlinx.android.synthetic.main.search_word_layout.view.*

sealed class SearchViewHolder(val view: View): RecyclerView.ViewHolder(view)

class SearchWordSourceViewHolder(view: View, private val handlers: SearchViewHolderHandlers):
        SearchViewHolder(view) {

    interface SearchViewHolderHandlers {
        fun onWordClicked(word: WordSource)
    }

    fun bind(word: WordSource) {
        when (word) {
            is SimpleWordSource -> bindSource(word.word.word, R.drawable.ic_round_search_outlined_24px, word)
            is FirestoreUserSource -> bindSource(word.userWord.word, R.drawable.ic_round_recent_outlined_24px, word)
            is SuggestSource -> bindSource(word.item.term, R.drawable.ic_round_smart_outlined_24px, word)
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

class SearchHeaderViewHolder(
        view: View,
        private val listener: BannerViewHolderListener
): SearchViewHolder(view), BannerViewHolder {

    fun bind(banner: Banner?) {
        view.banner.setBackgroundColor(view.context.getColorFromAttr(R.attr.surfaceBColor))
        setBanner(view.banner, banner, listener)
    }
}



