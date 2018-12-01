package com.wordsdict.android.util

import androidx.recyclerview.widget.DiffUtil

fun <T> emptyDiffItemCallback(): DiffUtil.ItemCallback<T> =
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = false
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = false
        }

