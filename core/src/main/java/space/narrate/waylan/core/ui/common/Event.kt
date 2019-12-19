package space.narrate.waylan.core.ui.common

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    var handled = false
        private set // Allow external read but not write

    /**
     * Operates on and consumes content if it's available.
     */
    fun withUnhandledContent(with: (T) -> Unit) {
        if (!handled) {
            handled = true
            with(content)
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peek(): T = content
}