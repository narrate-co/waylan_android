package space.narrate.waylan.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.TypedValue

fun Context.getColorFromAttr(colorAttr: Int): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}

fun Context.getFloatFromAttr(attr: Int): Float {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    val value = a.getFloat(0, 0F)
    a.recycle()
    return value
}

fun Context.getDimensionPixelSizeFromAttr(attr: Int): Int {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    val dimen = a.getDimensionPixelSize(0, 0)
    a.recycle()
    return dimen
}

val Context.displayHeightPx: Int
    get() = resources.displayMetrics.heightPixels

val Context.displayHeightDp: Float
    get() = resources.displayMetrics.heightPixels / resources.displayMetrics.density

fun Context.getStringOrNull(res: Int?): String? {
    return if (res == null) null else getString(res)
}

/**
 * Launch the user's default email client directly into a newly composed email.
 *
 * @param toEmail The addressee's email address
 * @param subject The email titleRes
 * @param body The email's content
 * @param shareTitle The titleRes of the share picker sheet the client will be offered to choose
 *  their desired email client
 * @throws ActivityNotFoundException If the user doesn't have an email client installed, this
 *  method will throw an ActivityNotFound exception
 */
@Throws(ActivityNotFoundException::class)
fun Context.launchEmail(toEmail: String, subject: String = "", body: String = "") {
    val intent = Intent(Intent.ACTION_SENDTO)
    val mailTo = "mailto:$toEmail?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
    intent.data = Uri.parse(mailTo)
    startActivity(intent)
}

/**
 * Open a [url] with the default browser
 */
fun Context.launchWebsite(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

