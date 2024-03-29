package space.narrate.waylan.core.data

/**
 * A class to help return values from functions and propagate errors.
 */
sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    val isSuccessful: Boolean
        get() = this is Success<*>

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Result::Success[data=$data]"
            is Error -> "Result::Error[exception=$exception]"
        }
    }

}
