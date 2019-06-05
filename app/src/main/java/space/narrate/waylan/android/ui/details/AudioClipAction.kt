package space.narrate.waylan.android.ui.details

sealed class AudioClipAction {
    data class Play(val url: String) : AudioClipAction()
    object Stop : AudioClipAction()
}