package com.words.android.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.card.MaterialCardView
import com.words.android.R
import com.words.android.data.disk.mw.Definition
import com.words.android.data.disk.mw.Word
import com.words.android.util.fromHtml
import kotlinx.android.synthetic.main.details_source_card_layout.view.*

class MerriamWebsterDefinitionsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.details_source_card_layout, this)
    }

    fun setDefinitions(word: Word?, definitions: List<Definition>?) {
        if (word == null || definitions == null || definitions.isEmpty()) {
            println("setMerriamWebsterWord::unavailable. word = $word, definitions = $definitions")
            visibility = View.GONE
            sourceCardContainer.removeAllViews()
            return
        }

        sourceCardContainer.removeAllViews()
        visibility = View.VISIBLE

        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        sourceCardContainer.addView(createPartOfSpeechView(sb.toString()))
        definitions.flatMap { it.definitions }.forEach {
            sourceCardContainer.addView(createMwDefinitionView(it.def))
        }

        //TODO add examples

        //TODO add synonyms
    }

     private fun createMwDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, this, false) as AppCompatTextView
        textView.text = def.fromHtml
        return textView
    }

    private fun createPartOfSpeechView(text: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_part_of_speech_layout, this, false) as AppCompatTextView
        textView.text = text
        return textView
    }
}

