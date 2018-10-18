package com.words.android.ui.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.card.MaterialCardView
import com.words.android.R
import com.words.android.data.disk.mw.Definition
import com.words.android.data.disk.mw.Word
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.service.AudioClipService
import com.words.android.service.AudioController
import com.words.android.util.contentEquals
import com.words.android.util.fromHtml
import com.words.android.util.toRelatedChip
import kotlinx.android.synthetic.main.merriam_webster_card_layout.view.*

class MerriamWebsterCard @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "MerriamWebsterDefinitionView"
    }

    interface MerriamWebsterViewListener {
        fun onRelatedWordClicked(word: String)
        fun onAudioClipError(message: String)
    }

    data class DefinitionGroup(var word: Word, var definitions: List<Definition>, var viewGroup: LinearLayout)

    private var currentWordId: String = ""
    private var definitionGroups: MutableList<DefinitionGroup> = mutableListOf()

    private var listener: MerriamWebsterViewListener? = null

    init {
        View.inflate(context, R.layout.merriam_webster_card_layout, this)
        visibility = View.GONE
        progressBar.visibility = View.INVISIBLE
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerAudioStateDispatchReceiver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentWordId = ""
        unregisterAudioStateDispatchReceiver()
    }


    fun clear() {
        println("$TAG::clear")
        currentWordId = ""
        definitionGroups = mutableListOf()
        definitionsContainer.removeAllViews()
        relatedWordsChipGroup.removeAllViews()
        audioImageView.setOnClickListener {  }
    }

    private val audioStateDispatchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val audioStateDispatch: AudioClipService.AudioStateDispatch = intent.getSerializableExtra(AudioClipService.BROADCAST_AUDIO_STATE_EXTRA_STATE) as AudioClipService.AudioStateDispatch
            val url = intent.getStringExtra(AudioClipService.BROADCAST_AUDIO_STATE_EXTRA_URL)
            val message = intent.getStringExtra(AudioClipService.BROADCAST_AUDIO_STATE_EXTRA_MESSAGE)

            setAudioIcon(audioStateDispatch)

            when (audioStateDispatch) {
                AudioClipService.AudioStateDispatch.ERROR -> listener?.onAudioClipError(message)
                AudioClipService.AudioStateDispatch.LOADING,
                AudioClipService.AudioStateDispatch.STOPPED,
                AudioClipService.AudioStateDispatch.PREPARED,
                AudioClipService.AudioStateDispatch.PLAYING -> setAudioIcon(audioStateDispatch)
            }
        }
    }

    private fun registerAudioStateDispatchReceiver() {
        LocalBroadcastManager.getInstance(context).registerReceiver(audioStateDispatchReceiver, IntentFilter(AudioClipService.BROADCAST_AUDIO_STATE_DISPATCH))
    }

    private fun unregisterAudioStateDispatchReceiver() {
        AudioController.stop(context)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(audioStateDispatchReceiver)
    }


    fun setWordAndDefinitions(entries: List<WordAndDefinitions>?) {
        if (entries == null || entries.isEmpty()) {
            clear()
            visibility = View.GONE
            return
        }

        val newWordId = getListWordAndDefId(entries)
        if (currentWordId != newWordId) {
            clear()
            currentWordId = newWordId
        }

        //set audio clip
        setAudio(entries.firstOrNull()?.word)

        //add entries
        entries.forEach {
            setWord(it.word)
            setDefinitions(it.word, it.definitions)
        }

    }

    private fun getListWordAndDefId(entries: List<WordAndDefinitions>): String {
        return entries.asSequence().map { it.word?.word }.firstOrNull() ?: ""
    }

    private val audioStopClickListener = OnClickListener { AudioController.stop(context) }

    private var audioPlayClickListener = OnClickListener {  }


    private fun setAudio(word: Word?) {
        if (word == null || word.sound.wav.isBlank()) return

        audioImageView.setImageResource(R.drawable.ic_round_play_arrow_24px)

        var fileName = word.sound.wav.removeSuffix(".wav")
        val url = if (fileName.isNotBlank()) "http://media.merriam-webster.com/audio/prons/en/us/mp3/${fileName.toCharArray().firstOrNull() ?: "a"}/$fileName.mp3" else ""
//        val url = "error" //error url
        println("$TAG::setAudio - url = $url")
        audioPlayClickListener = OnClickListener { AudioController.play(context, url) }

        audioImageView.setOnClickListener(audioPlayClickListener)
    }


    private fun setAudioIcon(state: AudioClipService.AudioStateDispatch) {
        when (state) {
            AudioClipService.AudioStateDispatch.LOADING -> {
                audioImageView.setImageResource(R.drawable.ic_round_stop_24px)
                audioImageView.setOnClickListener(audioStopClickListener)
                progressBar.visibility = View.VISIBLE
                underline.visibility = View.INVISIBLE
            }
            AudioClipService.AudioStateDispatch.PREPARED,
            AudioClipService.AudioStateDispatch.PLAYING -> {
                audioImageView.setImageResource(R.drawable.ic_round_stop_24px)
                audioImageView.setOnClickListener(audioStopClickListener)
                progressBar.visibility = View.INVISIBLE
                underline.visibility = View.VISIBLE
            }
            AudioClipService.AudioStateDispatch.STOPPED -> {
                audioImageView.setImageResource(R.drawable.ic_round_play_arrow_24px)
                audioImageView.setOnClickListener(audioPlayClickListener)
                progressBar.visibility = View.INVISIBLE
                underline.visibility = View.VISIBLE
            }
        }
    }


    private fun setWord(word: Word?) {
        if (word == null) return

        //TODO make this diffing smarter
        if (word.relatedWords.isNotEmpty()) {
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
            if (existingGroup.word != word || !existingGroup.definitions.containsAll(definitions)) {
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

