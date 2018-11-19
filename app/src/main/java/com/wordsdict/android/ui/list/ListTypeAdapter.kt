package com.wordsdict.android.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.wordsdict.android.R
import com.wordsdict.android.data.repository.FirestoreGlobalSource
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.util.Banner
import java.lang.IllegalStateException

class ListTypeAdapter(
        private val listener: ListTypeAdapter.ListTypeListener
):
        ListAdapter<WordSource,
        ListViewHolder>(diffCallback),
        ListWordSourceViewHolder.ListWordSourceListener,
        ListHeaderViewHolder.ListHeaderListener {


    interface ListTypeListener {
        fun onWordClicked(word: String)
        fun onBannerClicked(banner: Banner)
        fun onBannerTopButtonClicked(banner: Banner)
        fun onBannerBottomButtonClicked(banner: Banner)
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

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_WORDSOURCE = 1
    }

    private var banner: Banner? = null

    private fun getHeaderOffset(): Int {
        return if (banner == null) 0 else 1
    }

    fun setBanner(banner: Banner?) {
        if (this.banner == null && banner != null) {
            //we're inserting the header
            this.banner = banner
            notifyItemInserted(0)
        } else if (this.banner != null && banner == null) {
            //we're removing the header
            this.banner = banner
            notifyItemRemoved(0)
        } else if (this.banner != null && banner != null) {
            //we're changing the header
            this.banner = banner
            notifyItemChanged(0)
        } // else nothing has changed. The banner was, and still is, null.
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && banner != null) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_WORDSOURCE
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + getHeaderOffset()
    }

    override fun getItem(position: Int): WordSource {
        return super.getItem(position - getHeaderOffset())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> ListHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_banner_layout, parent, false), this)
            VIEW_TYPE_WORDSOURCE -> ListWordSourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false), this)
            else -> throw IllegalStateException("Unsupported type being inflated ")
        }
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        when (holder) {
            is ListHeaderViewHolder -> holder.bind(banner)
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

