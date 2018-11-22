package com.wordsdict.android.util

import androidx.recyclerview.widget.DiffUtil

class EmptyDiffItemCallback<T>: DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return false
    }

}