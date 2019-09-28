package space.narrate.waylan.core.ui.common

data class SnackbarModel(
    val textRes: Int,
    val length: Int = LENGTH_SHORT,
    val isError: Boolean = false,
    val actionRes: Int? = null
) {
    companion object {
        const val LENGTH_INDEFINITE = -2
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0
    }
}