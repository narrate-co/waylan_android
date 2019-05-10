package space.narrate.words.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.wordset.Synonym
import java.util.*

val String.fromHtml: Spanned
    @SuppressLint("NewApi")
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }

infix fun <T> Collection<T>.contentEquals(collection: Collection<T>?): Boolean
    = collection?.let { this.size == it.size && this.containsAll(collection) } ?: false

val <T> Stack<T>?.peekOrNull: T?
    get() = if (this == null || isEmpty()) null else peek()

fun Synonym.toChip(
    context: Context,
    chipGroup: ChipGroup?,
    onClick: ((synonym: Synonym) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
            R.layout.details_chip_layout,
            chipGroup,
            false
    ) as Chip
    chip.text = this.synonym
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun String.toRelatedChip(
        context: Context,
        chipGroup: ChipGroup?,
        onClick: ((word: String) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
            R.layout.details_related_chip_layout,
            chipGroup,
            false
    ) as Chip
    val underlinedString = SpannableString(this)
    underlinedString.setSpan(UnderlineSpan(),0,this.length,0)
    chip.text = underlinedString
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun Activity.hideSoftKeyboard() {
    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    im.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
}

fun Activity.showSoftKeyboard(view: View) {
    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}