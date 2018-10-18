package com.words.android.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.words.android.R
import com.words.android.data.repository.WordSource
import kotlinx.android.synthetic.main.search_word_layout.view.*

class WordViewHolder(private val view: View, private val handlers: WordViewHolderHandlers):
        RecyclerView.ViewHolder(view) {

    interface WordViewHolderHandlers {
        fun onWordClicked(word: String)
    }

    fun bind(word: WordSource) {
        when (word) {
            is WordSource.SimpleWordSource -> bindSource(word.word.word, R.drawable.ic_round_search_black_24px)
            is WordSource.FirestoreUserSource -> bindSource(word.userWord.word, R.drawable.ic_round_access_time_black_24px)
            else -> clear()
        }
    }

    private fun bindSource(word: String, imgRes: Int) {
        view.wordText.text = word
        view.wordIcon.setImageResource(imgRes)
        view.container.setOnClickListener { handlers.onWordClicked(word) }
    }

    fun clear() {
        view.wordText.text = ""
        view.wordIcon.setImageDrawable(null)
    }


}



