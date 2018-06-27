package com.words.android.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.R
import com.words.android.data.Word
import com.words.android.databinding.WordLayoutBinding

class WordsAdapter(private val handlers: WordAdapterHandlers): ListAdapter<Word, WordViewHolder>(diffCallback), WordViewHolder.WordViewHolderHandlers {

    companion object {
        val diffCallback: DiffUtil.ItemCallback<Word> = object : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean =
                    oldItem.word == newItem.word

            override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean =
                    oldItem.popularity == newItem.popularity && oldItem.modified == newItem.modified
        }
    }

    interface WordAdapterHandlers {
        fun onWordClicked(word: Word)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding: WordLayoutBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.word_layout, parent,
                false)
        return WordViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onWordClicked(pos: Int) {
        handlers.onWordClicked(getItem(pos))
    }

}