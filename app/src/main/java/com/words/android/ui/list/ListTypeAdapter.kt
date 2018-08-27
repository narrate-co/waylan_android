package com.words.android.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.R
import com.words.android.data.repository.Word

class ListTypeAdapter(private val listener: ListTypeAdapter.ListTypeListener): ListAdapter<Word, ListTypeViewHolder>(diffCallback), ListTypeViewHolder.ListTypeViewHolderListener {

    interface ListTypeListener {
        fun onWordClicked(word: String)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(o: Word, n: Word): Boolean {
                return o.userWord?.id == n.userWord?.id
            }

            override fun areContentsTheSame(o: Word, n: Word): Boolean {
                return o.userWord?.word == n.userWord?.word &&
                        o.userWord?.defPreview == n.userWord?.defPreview &&
                        o.userWord?.synonymPreview == n.userWord?.synonymPreview &&
                        o.userWord?.partOfSpeechPreview == n.userWord?.partOfSpeechPreview
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTypeViewHolder {
        return ListTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false), this)
    }

    override fun onBindViewHolder(holder: ListTypeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onWordClicked(word: String) {
        listener.onWordClicked(word)
    }
}

