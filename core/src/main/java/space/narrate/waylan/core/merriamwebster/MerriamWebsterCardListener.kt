package space.narrate.waylan.core.merriamwebster

/**
 * An interface which allows clients which display a MerriamWebsterCardView to listen
 * to events which happen on that view when the client does not depend on :merriamwebster.
 */
interface MerriamWebsterCardListener {
    fun onMwRelatedWordClicked(word: String)
    fun onMwSuggestionWordClicked(word: String)
    fun onMwAudioPlayClicked(url: String?)
    fun onMwAudioStopClicked()
    fun onMwAudioClipError(messageRes: Int)
    fun onMwPermissionPaneDetailsClicked()
    fun onMwPermissionPaneDismissClicked()
}