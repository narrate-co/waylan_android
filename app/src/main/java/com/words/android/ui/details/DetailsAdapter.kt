package com.words.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.words.android.data.repository.Word

class DetailsAdapter(private val listener: DetailsAdapter.Listener): ListAdapter<DetailsComponent, DetailsComponentViewHolder>(diffCallback), DetailsComponentListener {

    interface Listener {
        fun onRelatedWordClicked(relatedWord: String)
        fun onSynonymChipClicked(synonym: String)
        fun onAudioClipError(message: String)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DetailsComponent>() {
            override fun areItemsTheSame(oldItem: DetailsComponent, newItem: DetailsComponent): Boolean {
                return oldItem.equalTo(newItem)
            }
            override fun areContentsTheSame(oldItem: DetailsComponent, newItem: DetailsComponent): Boolean {
                return oldItem.contentsSameAs(newItem)
            }

            override fun getChangePayload(oldItem: DetailsComponent, newItem: DetailsComponent): Any? {
                return oldItem.getChangePayload(newItem)
            }
        }
    }

    fun submitWord(word: Word?) {
        submitList(word.toComponentsList)
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.number

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsComponentViewHolder {
        return DetailsComponentViewHolder.createViewHolder(parent, viewType, this)
    }

    override fun onBindViewHolder(holder: DetailsComponentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //TODO override onBindViewHolder w/ payload

    override fun onSynonymChipClicked(synonym: String) {
        listener.onSynonymChipClicked(synonym)
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        listener.onRelatedWordClicked(relatedWord)
    }

    override fun onAudioClipError(message: String) {
        listener.onAudioClipError(message)
    }

    private val Word?.toComponentsList: List<DetailsComponent>
        get() {
            if (this == null) return emptyList()

            val list = mutableListOf<DetailsComponent>()
            list.add(DetailsComponent.TitleComponent(this))
            println("toComponentsList - TitleComponent - ${this.dbWord?.word}")
            if (mwEntry.isNotEmpty()) list.add(DetailsComponent.MerriamWebsterComponent(this))
            if (dbMeanings.isNotEmpty()) list.add(DetailsComponent.WordsetComponent(this))
            if (dbMeanings.map { it.examples }.toList().isNotEmpty()) list.add(DetailsComponent.ExamplesComponent(this))

            return list
        }
}