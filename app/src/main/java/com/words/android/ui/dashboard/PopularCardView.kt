package com.words.android.ui.dashboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.words.android.R
import com.words.android.data.disk.Synonym
import com.words.android.data.repository.Word
import com.words.android.util.toChip
import kotlinx.android.synthetic.main.dashboard_popular_card_layout.view.*
import org.threeten.bp.OffsetDateTime
import java.util.*

class PopularCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.materialCardViewStyle
): MaterialCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.dashboard_popular_card_layout, this)
    }

    interface PopularCardListener {
        fun onPopularCardClicked(word: Word)
        fun onPopularSynonymClicked(id: String)
    }

    var listener: PopularCardListener? = null

    fun setPopularCardListener(listener: PopularCardListener) {
        this.listener = listener
    }

    fun removePopularCardListener() {
        this.listener = null
    }

    fun setWord(word: Word) {
        word.userWord?.let {
            popularWord.text = it.word
        }

        popularChipGroup.removeAllViews()
        word.userWord?.synonymPreview?.forEach {
            popularChipGroup.addView(Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now()).toChip(context, popularChipGroup) {
                listener?.onPopularSynonymClicked(it.synonym)
            })
        }


        word.userWord?.defPreview?.map { it.key }?.firstOrNull()?.let {
            popularDefinition.text = it
        }


        //TODO handle on card click
        popularCard.setOnClickListener {
            listener?.onPopularCardClicked(word)
        }

    }
}

