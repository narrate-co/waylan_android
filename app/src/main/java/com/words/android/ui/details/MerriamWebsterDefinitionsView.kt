package com.words.android.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.card.MaterialCardView
import com.words.android.R
import com.words.android.data.disk.mw.Definition
import com.words.android.data.disk.mw.Word
import com.words.android.util.contentEquals
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

    companion object {
        private const val TAG = "MerriamWebsterDefinitionView"
    }

    private var lastDefinitionList: List<Definition> = emptyList()

    fun clear() {
        lastDefinitionList = emptyList()
        partOfSpeech.text = ""
    }


    fun setWord(word: Word?) {
        if (word == null) return

        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        if (partOfSpeech.text != sb.toString()) {
            partOfSpeech.text = sb.toString()
        }

    }

    fun setDefinitions(definitions: List<Definition>?) {
        if (definitions != null && !lastDefinitionList.contentEquals(definitions)) {

            println("$TAG::setDefinitions - different. LAST: $lastDefinitionList | NEW: $definitions")
            //we've got new definitions!
            lastDefinitionList = definitions

            definitionsContainer.removeAllViews()
            //TODO handle visibility in another place - take care of words w/o MW entries
//            visibility = View.VISIBLE

            definitions.flatMap { it.definitions }.forEachIndexed { i, it ->
                definitionsContainer.addView(createMwDefinitionView(it.def))
            }
        } else {
            //definitions is either null or the same as what's already added
            println("$TAG::setDefinitions - null or the same as what's already set. LAST: $lastDefinitionList | NEW: $definitions")
        }


        //TODO add examples

        //TODO add synonyms
    }

     private fun createMwDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, this, false) as AppCompatTextView
        textView.text = def.fromHtml
        return textView
    }

}

