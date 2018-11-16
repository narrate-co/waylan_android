package com.words.android.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.words.android.R
import com.words.android.data.repository.FirestoreUserSource
import com.words.android.data.repository.SimpleWordSource
import com.words.android.data.repository.SuggestSource
import com.words.android.data.repository.WordSource
import kotlinx.android.synthetic.main.search_word_layout.view.*

class SearchViewHolder(private val view: View, private val handlers: SearchViewHolderHandlers):
        RecyclerView.ViewHolder(view) {

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



