package space.narrate.waylan.core.merriamwebster

interface MerriamWebsterCardListener {
    fun onMwRelatedWordClicked(word: String)
    fun onMwSuggestionWordClicked(word: String)
    fun onMwAudioPlayClicked(url: String?)
    fun onMwAudioStopClicked()
    fun onMwAudioClipError(messageRes: Int)
    fun onMwPermissionPaneDetailsClicked()
    fun onMwPermissionPaneDismissClicked()
}