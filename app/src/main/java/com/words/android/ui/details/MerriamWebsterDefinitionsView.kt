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
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.util.contentEquals
import com.words.android.util.fromHtml
import com.words.android.util.toRelatedChip
import kotlinx.android.synthetic.main.details_source_card_layout.view.*

class MerriamWebsterDefinitionsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.details_source_card_layout, this)
        visibility = View.GONE
    }

    companion object {
        private const val TAG = "MerriamWebsterDefinitionView"
    }

    interface MerriamWebsterViewListener {
        fun onRelatedWordClicked(word: String)
    }

    data class DefinitionGroup(var word: Word, var definitions: List<Definition>, var viewGroup: LinearLayout)

    private var definitionGroups: MutableList<DefinitionGroup> = mutableListOf()

    private var listener: MerriamWebsterViewListener? = null

    fun clear() {
        println("$TAG::clear")
        definitionGroups = mutableListOf()
        definitionsContainer.removeAllViews()
        visibility = View.GONE
    }

    fun setWordAndDefinitions(entries: List<WordAndDefinitions>?) {
        //TODO show card with relevant words even if definitions are null
        println("$TAG::setWordAndDefinitions - ${entries?.map { it.word }}")
        //TODO add ability to show all entries

        if (entries == null || entries.isEmpty()) {
            clear()
            return
        }

        entries.forEach {
            setWord(it.word)
            setDefinitions(it.word, it.definitions)
        }

    }


    private fun setWord(word: Word?) {

        if (word == null) return

        //TODO make this diffing smarter
        if (word.relatedWords.isNotEmpty()) {
            relatedWordsChipGroup.removeAllViews()
            word.relatedWords.forEach {
                relatedWordsChipGroup?.addView(it.toRelatedChip(context, relatedWordsChipGroup) {
                    listener?.onRelatedWordClicked(it)
                })
            }
            relatedWordsHeader.visibility = View.VISIBLE
            relatedWordsHorizontalScrollView.visibility = View.VISIBLE
        } else {
            relatedWordsChipGroup.removeAllViews()
            relatedWordsHeader.visibility = View.GONE
            relatedWordsHorizontalScrollView.visibility = View.GONE
        }
    }

    private fun setDefinitions(word: Word?, definitions: List<Definition>?) {

        //if definitionGroups does not contain word + defs
        //TODO add

        //if definitionGroups does contain word + defs
        //TODO remove + add
        if (word == null || definitions == null || definitions.isEmpty()) return

        val existingGroup = definitionGroups.firstOrNull { it.word.id == word.id }
        if (existingGroup == null) {
            //this is a new group. create and add it
            val newGroup = createDefinitionGroup(word, definitions)
            definitionGroups.add(newGroup)
            newGroup.viewGroup.addView(createPartOfSpeechView(newGroup.word))
            newGroup.definitions.flatMap { it.definitions }.forEach { newGroup.viewGroup.addView(createMwDefinitionView(it.def)) }
            definitionsContainer.addView(newGroup.viewGroup)
        } else {
            //this is an existing group. diff it
            if (existingGroup.word != word || !existingGroup.definitions.contentEquals(definitions)) {
                //change part of speech
                existingGroup.word = word
                existingGroup.definitions = definitions
                existingGroup.viewGroup.removeAllViews()
                existingGroup.viewGroup.addView(createPartOfSpeechView(existingGroup.word))
                existingGroup.definitions.flatMap { it.definitions }.forEach { existingGroup.viewGroup.addView(createMwDefinitionView(it.def)) }
            }
        }

        visibility = View.VISIBLE


        //TODO add examples

        //TODO add synonyms
    }

    fun addListener(listener: MerriamWebsterViewListener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    private fun createDefinitionGroup(word: Word, definitions: List<Definition>): DefinitionGroup {
        val group: LinearLayout = LayoutInflater.from(context).inflate(R.layout.details_card_definition_group_layout, this, false) as LinearLayout
        return DefinitionGroup(word, definitions, group)
    }

     private fun createMwDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, this, false) as AppCompatTextView
        textView.text = def.fromHtml
        return textView
    }

    private fun createPartOfSpeechView(word: Word): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_part_of_speech_layout, this, false) as AppCompatTextView
        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        textView.text = sb.toString()
        return textView
    }

}

