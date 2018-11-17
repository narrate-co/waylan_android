package com.wordsdict.android.ui.details

interface DetailsComponentListener {
    fun onSynonymChipClicked(synonym: String)
    fun onRelatedWordClicked(relatedWord: String)
    fun onAudioClipError(message: String)
    fun onMerriamWebsterDismissClicked()
}
