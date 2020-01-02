package space.narrate.waylan.core.merriamwebster

import space.narrate.waylan.core.ui.common.AddOnListener

/**
 * An interface which allows clients which display a MerriamWebsterCardView to listen
 * to events which happen on that view when the client does not depend on :merriamwebster.
 */
interface MerriamWebsterCardListener : AddOnListener {
    fun onMwRelatedWordClicked(word: String)
    fun onMwSuggestionWordClicked(word: String)
    fun onMwAudioPlayClicked(url: String?)
    fun onMwAudioStopClicked()
    fun onMwAudioClipError(messageRes: Int)
}