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
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.util.contentEquals
import com.words.android.util.fromHtml
import com.words.android.util.toChip
import com.words.android.util.toRelatedChip
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
        println("$TAG::clear")
        lastDefinitionList = emptyList()
        definitionsContainer.removeAllViews()
        partOfSpeech.text = ""
        visibility = View.GONE
    }

    fun setWordAndDefinitions(entries: List<WordAndDefinitions>?) {
        //TODO show card with relevant words even if definitions are null
        println("$TAG::setWordAndDefinitions - ${entries?.map { it.word }}")
        //TODO add ability to show all entries
        entries?.firstOrNull()?.let {
            if (it.word == null && (it.definitions == null || it.definitions.isEmpty())) {
                //NO MERRIAM WEBSTER WORD
                //unless it's been erased from the db to be replaced by the new api retrieved word
                //TODO avoid clearing because an entry is being replaced in the DB
                clear()
            } else {
                setWord(it.word)
                setDefinitions(it.definitions)
            }
        } ?: clear()

    }


    private fun setWord(word: Word?) {

        if (word == null) return

        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        if (partOfSpeech.text != sb.toString()) {
            partOfSpeech.text = sb.toString()
        }

        //TODO make this diffing smarter
        if (word.relatedWords.isNotEmpty()) {
            relatedWordsChipGroup.removeAllViews()
            word.relatedWords.forEach {
                relatedWordsChipGroup?.addView(it.toRelatedChip(context, relatedWordsChipGroup) {
                    //TODO add chip onClick listener callback invocation
                })
            }
            relatedWordsHeader.visibility = View.VISIBLE
            relatedWordsChipGroup.visibility = View.VISIBLE
        } else {
            relatedWordsChipGroup.removeAllViews()
            relatedWordsHeader.visibility = View.GONE
            relatedWordsChipGroup.visibility = View.GONE
        }
    }

    private fun setDefinitions(definitions: List<Definition>?) {
        if (definitions != null && definitions.isNotEmpty() && !lastDefinitionList.contentEquals(definitions)) {

            println("$TAG::setDefinitions - different. LAST: $lastDefinitionList | NEW: $definitions")
            //we've got new definitions!
            lastDefinitionList = definitions

            definitionsContainer.removeAllViews()

            definitions.flatMap { it.definitions }.forEachIndexed { i, it ->
                definitionsContainer.addView(createMwDefinitionView(it.def))
            }

            visibility = View.VISIBLE
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

