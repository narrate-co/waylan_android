package space.narrate.waylan.core.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.util.*

val isAtLeastQ: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

@Suppress("DEPRECATION")
val String.fromHtml: Spanned
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }

infix fun <T> Collection<T>.contentEquals(collection: Collection<T>?): Boolean
    = collection?.let { this.size == it.size && this.containsAll(collection) } ?: false

val <T> Stack<T>?.peekOrNull: T?
    get() = if (this == null || isEmpty()) null else peek()

fun Activity.hideIme() {
    val view = currentFocus ?: View(this)
    view.hideIme()
}
