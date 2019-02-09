package com.wordsdict.android.ui.details

/**
 * A synthesized listener for all user interaction events that can occur accross all
 * [DetailsComponentViewHolder]s
 */
interface DetailsComponentListener {
    fun onSynonymChipClicked(synonym: String)
    fun onRelatedWordClicked(relatedWord: String)
    fun onSuggestionWordClicked(suggestionWord: String)
    fun onAudioPlayClicked(url: String?)
    fun onAudioStopClicked()
    fun onAudioClipError(message: String)
    fun onMerriamWebsterDismissClicked()
}

