package space.narrate.words.android.ui.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.merriam_webster_card_layout.view.*
import space.narrate.words.android.Navigator
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.mw.Definition
import space.narrate.words.android.data.disk.mw.PermissiveWordsDefinitions
import space.narrate.words.android.data.disk.mw.Word
import space.narrate.words.android.data.disk.mw.WordAndDefinitions
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.util.fromHtml
import space.narrate.words.android.util.toRelatedChip

/**
 * TODO refactor/rewrite
 */
class MerriamWebsterCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "MerriamWebsterDefinitionView"
    }

    interface MerriamWebsterViewListener {
        fun onRelatedWordClicked(word: String)
        fun onSuggestionWordClicked(word: String)
        fun onAudioPlayClicked(url: String?)
        fun onAudioStopClicked()
        fun onAudioClipError(message: String)
        fun onDismissCardClicked()
    }

    data class DefinitionGroup(
            var word: Word,
            var definitions: List<Definition>,
            var viewGroup: LinearLayout
    )

    private var currentWordId: String = ""
    private var definitionGroups: MutableList<DefinitionGroup> = mutableListOf()

    private var listener: MerriamWebsterViewListener? = null

    init {
        View.inflate(context, R.layout.merriam_webster_card_layout, this)
        visibility = View.GONE
        underline.visibility = View.INVISIBLE
        textLabel.setOnClickListener {
            Navigator.launchSettings(context)
        }
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
        currentWordId = ""
        definitionGroups = mutableListOf()
        definitionsContainer.removeAllViews()
        relatedWordsChipGroup.removeAllViews()
        audioImageView.setOnClickListener {  }
    }

    private val audioStateDispatchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val audioState: AudioClipHelper.AudioState = intent.getSerializableExtra(AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_STATE) as AudioClipHelper.AudioState
            val message = intent.getStringExtra(AudioClipHelper.BROADCAST_AUDIO_STATE_EXTRA_MESSAGE)

            setAudioIcon(audioState)

            when (audioState) {
                AudioClipHelper.AudioState.ERROR -> listener?.onAudioClipError(message)
                AudioClipHelper.AudioState.LOADING,
                AudioClipHelper.AudioState.STOPPED,
                AudioClipHelper.AudioState.PREPARED,
                AudioClipHelper.AudioState.PLAYING -> setAudioIcon(audioState)
            }
        }
    }

    private fun registerAudioStateDispatchReceiver() {
        LocalBroadcastManager.getInstance(context).registerReceiver(audioStateDispatchReceiver, IntentFilter(AudioClipHelper.BROADCAST_AUDIO_STATE_DISPATCH))
    }

    private fun unregisterAudioStateDispatchReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(audioStateDispatchReceiver)
    }

    fun setWordAndDefinitions(wordsAndDefinitions: PermissiveWordsDefinitions?) {
        if (wordsAndDefinitions?.entries == null || wordsAndDefinitions.entries.isEmpty()) {
            clear()
            visibility = View.GONE
            return
        }

        val newWordId = getListWordAndDefId(wordsAndDefinitions.entries)
        if (currentWordId != newWordId) {
            clear()
            currentWordId = newWordId
        }

        if (wordsAndDefinitions.user?.merriamWebsterState?.isValid == true) {
            setFieldsGranted(wordsAndDefinitions)
        } else {
            setFieldsDenied(wordsAndDefinitions.user)
        }
    }

    private fun setFieldsGranted(wordsAndDefinitions: PermissiveWordsDefinitions) {
        //set audio clip
        permissionContainer.visibility = View.GONE

        setAudio(wordsAndDefinitions.entries.firstOrNull()?.word)


        setTextLabel(wordsAndDefinitions.user)

        //add entries
        wordsAndDefinitions.entries.forEach {
            setRelatedWords(it.word)
            setDefinitions(it.word, it.definitions)
        }

        visibility = View.VISIBLE
    }

    private fun setFieldsDenied(user: User?) {
        audioImageView.visibility = View.GONE
        underline.visibility = View.GONE
        definitionsContainer.visibility = View.GONE
        relatedWordsHeader.visibility = View.GONE
        relatedWordsHorizontalScrollView.visibility = View.GONE

        val state = user?.merriamWebsterState ?: PluginState.None()
        when (state) {
            is PluginState.None -> textLabel.text = "Plugin available"
            is PluginState.FreeTrial -> textLabel.text = "Free trial expired"
            is PluginState.Purchased -> textLabel.text = "Plugin expired"
        }
        textLabel.visibility = View.VISIBLE

        permissionContainer.visibility = View.VISIBLE
        permissionContainer.topButton.setOnClickListener {
            Navigator.launchSettings(context)
        }
        permissionContainer.bottomButton.setOnClickListener {
            listener?.onDismissCardClicked()
        }

        visibility = View.VISIBLE
    }

    private fun getListWordAndDefId(entries: List<WordAndDefinitions>): String {
        return entries.asSequence().map { it.word?.word }.firstOrNull() ?: ""
    }

    private val audioStopClickListener = OnClickListener {
        listener?.onAudioStopClicked()
    }

    private var audioPlayClickListener = OnClickListener {  }


    private fun setAudio(word: Word?) {
        // TODO create a way to pass multiple wavs's trying until one plays
        val wavFile = word?.sound?.map { it.wavs }?.flatten()?.firstOrNull() ?: ""

        audioImageView.setImageResource(R.drawable.ic_round_play_arrow_24px)

        var fileName = wavFile.removeSuffix(".wav")
        val url = if (fileName.isNotBlank()) "https://media.merriam-webster.com/audio/prons/en/us/mp3/${fileName.toCharArray().firstOrNull() ?: "a"}/$fileName.mp3" else ""
//        val url = "https://media.merriam-webster.com/audio/prons/en/us/mp3/e/example.mp3" //error url
        audioPlayClickListener = OnClickListener {
            listener?.onAudioPlayClicked(url)
        }

        audioImageView.setOnClickListener(audioPlayClickListener)
        audioImageView.visibility = View.VISIBLE
        underline.visibility = View.VISIBLE
    }


    private fun setAudioIcon(state: AudioClipHelper.AudioState) {
        when (state) {
            AudioClipHelper.AudioState.LOADING -> {
                audioImageView.setImageResource(R.drawable.ic_round_stop_24px)
                audioImageView.setOnClickListener(audioStopClickListener)
                underline.startProgress()
            }
            AudioClipHelper.AudioState.PREPARED,
            AudioClipHelper.AudioState.PLAYING -> {
                audioImageView.setImageResource(R.drawable.ic_round_stop_24px)
                audioImageView.setOnClickListener(audioStopClickListener)
                underline.stopProgress()
            }
            AudioClipHelper.AudioState.STOPPED -> {
                audioImageView.setImageResource(R.drawable.ic_round_play_arrow_24px)
                audioImageView.setOnClickListener(audioPlayClickListener)
                underline.stopProgress()
            }
        }
    }

    private fun setTextLabel(user: User?) {
        val state = user?.merriamWebsterState
        when (state) {
            is PluginState.FreeTrial -> {
                if (state.isValid) {
                    textLabel.text = "Free trial: ${state.remainingDays}d"
                } else {
                    textLabel.text = "Free trial expired"
                }
                textLabel.visibility = View.VISIBLE
            }
            is PluginState.Purchased -> {
                if (!state.isValid) {
                    // show label
                    textLabel.text = "Plugin expired"
                    textLabel.visibility = View.VISIBLE
                } else if (state.remainingDays <= 7L) {
                    // hide label
                    textLabel.text = "Renew: ${state.remainingDays}d remaining"
                    textLabel.visibility = View.VISIBLE
                } else {
                    textLabel.visibility = View.GONE
                }
            }
            else -> textLabel.visibility = View.GONE
        }
    }


    private fun setRelatedWords(word: Word?) {
        if (word == null) return

        val wordsList = (word.relatedWords + word.suggestions).filterNot { it == word.word }.distinct()

        //TODO make this diffing smarter
        if (wordsList.isNotEmpty()) {
            wordsList.forEach {
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

        definitionsContainer.visibility = View.VISIBLE

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

