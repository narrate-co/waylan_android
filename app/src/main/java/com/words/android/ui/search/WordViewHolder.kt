package com.words.android.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.words.android.data.repository.Word
import com.words.android.databinding.WordLayoutBinding

class WordViewHolder(private val binding: WordLayoutBinding, private val handlers: WordViewHolderHandlers):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    interface WordViewHolderHandlers {
        fun onWordClicked(pos: Int)
    }

    fun bind(word: Word) {
        binding.word = word.dbWord ?: Word().dbWord
        binding.userWord = word.userWord
        binding.clickListener = this
        binding.executePendingBindings()
    }

    fun clear() {
        binding.word = null
        binding.userWord = null
        binding.executePendingBindings()
    }

    override fun onClick(view: View?) {
        handlers.onWordClicked(adapterPosition)
    }

}



