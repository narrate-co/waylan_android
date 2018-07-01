package com.words.android.ui.dashboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.words.android.R
import com.words.android.data.repository.Word
import kotlinx.android.synthetic.main.dashboard_list_card_layout.view.*
import kotlinx.android.synthetic.main.dashboard_list_card_word_item.view.*

class ListCardLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.materialCardViewStyle
): MaterialCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.dashboard_list_card_layout, this)
    }

    interface ListCardListener {
        fun onListWordClicked(id: String)
        fun onListMoreClicked()
    }

    var listener: ListCardListener? = null

    fun setListCardListener(listener: ListCardListener) {
        this.listener = listener
    }

    fun removeListCardListener() {
        this.listener = null
    }

    fun setCardIcon(drawable: Int) {
        listCardIcon.setImageDrawable(ContextCompat.getDrawable(context, drawable))
    }

    fun setWords(words: List<Word>) {
        listContainer.removeAllViews()
        words.mapNotNull { it.userWord }.take(3).forEach { userWord ->
            val itemView = LayoutInflater.from(context).inflate(R.layout.dashboard_list_card_word_item, listContainer, false)
            itemView.listWord.text = userWord.word
            itemView.listDefinition.text = userWord.defPreview.map { it.key }.firstOrNull() ?: ""
            itemView.setOnClickListener {
                listener?.onListWordClicked(userWord.id)
            }
            listContainer.addView(itemView)
        }

        listMore.setOnClickListener {
            listener?.onListMoreClicked()
        }
    }
}


