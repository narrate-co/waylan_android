package space.narrate.words.android.ui.details

sealed class AudioClipAction {
    data class Play(val url: String) : AudioClipAction()
    object Stop : AudioClipAction()
}