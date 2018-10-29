package com.words.android.ui.details

import com.words.android.data.disk.wordset.Synonym

interface DetailsComponentListener {
    fun onSynonymChipClicked(synonym: String)
    fun onRelatedWordClicked(relatedWord: String)
    fun onAudioClipError(message: String)
    fun onMerriamWebsterDismissClicked()
}

