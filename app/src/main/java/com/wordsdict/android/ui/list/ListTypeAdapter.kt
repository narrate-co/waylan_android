package com.wordsdict.android.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.wordsdict.android.R
import com.wordsdict.android.data.repository.FirestoreGlobalSource
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.ui.common.HeaderListAdapter
import java.lang.IllegalStateException

class ListTypeAdapter(
        private val listener: ListTypeAdapter.ListTypeListener
):
        HeaderListAdapter<WordSource, ListViewHolder, Banner>(diffCallback),
        ListWordSourceViewHolder.ListWordSourceListener,
        BannerViewHolderListener {


    interface ListTypeListener: BannerViewHolderListener {
        fun onWordClicked(word: String)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<WordSource>() {
            override fun areItemsTheSame(o: WordSource, n: WordSource): Boolean {
                if (o is FirestoreUserSource && n is FirestoreUserSource) return o.userWord.id == n.userWord.id
                if (o is FirestoreGlobalSource && n is FirestoreGlobalSource) return o.globalWord.id == n.globalWord.id
                return false
            }

            override fun areContentsTheSame(oldItem: WordSource, newItem: WordSource): Boolean {
                if (oldItem is FirestoreUserSource && newItem is FirestoreUserSource) return areUserWordContentsTheSame(oldItem, newItem)
                if (oldItem is FirestoreGlobalSource && newItem is FirestoreGlobalSource) return areGlobalWordContentsTheSame(oldItem, newItem)

                return false
            }

            fun areUserWordContentsTheSame(o: FirestoreUserSource, n: FirestoreUserSource): Boolean {
                return o.userWord.word == n.userWord.word &&
                        o.userWord.defPreview == n.userWord.defPreview &&
                        o.userWord.synonymPreview == n.userWord.synonymPreview &&
                        o.userWord.partOfSpeechPreview == n.userWord.partOfSpeechPreview
            }

            fun areGlobalWordContentsTheSame(o: FirestoreGlobalSource, n: FirestoreGlobalSource): Boolean {
                return o.globalWord.word == n.globalWord.word &&
                        o.globalWord.defPreview == n.globalWord.defPreview &&
                        o.globalWord.synonymPreview == n.globalWord.synonymPreview &&
                        o.globalWord.partOfSpeechPreview == n.globalWord.partOfSpeechPreview
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> ListHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_banner_layout, parent, false), this)
            VIEW_TYPE_ITEM -> ListWordSourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false), this)
            else -> throw IllegalStateException("Unsupported type being inflated ")
        }
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        when (holder) {
            is ListHeaderViewHolder -> holder.bind(getHeader())
            is ListWordSourceViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun onWordClicked(word: String) {
        listener.onWordClicked(word)
    }

    override fun onBannerClicked(banner: Banner) {
        listener.onBannerClicked(banner)
    }

    override fun onBannerTopButtonClicked(banner: Banner) {
        listener.onBannerTopButtonClicked(banner)
    }

    override fun onBannerBottomButtonClicked(banner: Banner) {
        listener.onBannerBottomButtonClicked(banner)
    }
}

