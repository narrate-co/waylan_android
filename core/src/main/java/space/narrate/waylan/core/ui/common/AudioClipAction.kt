package space.narrate.waylan.core.ui.common

sealed class AudioClipAction {
    data class Play(val url: String) : AudioClipAction()
    object Stop : AudioClipAction()
}