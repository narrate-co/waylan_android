package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.use
import space.narrate.waylan.core.R
import space.narrate.waylan.core.util.fadeThroughTransition
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.invisible
import space.narrate.waylan.core.util.visible

/**
 * A compound ViewGroup which displays an preference item containing a titleRes, a description and
 * an optional checkbox.
 *
 * @property title The main titleRes of the preference.
 * @property desc A short sentence describing what the preference does or the current value of the
 *  preference (ie. a preference with the titleRes Orientation might choose to use the description to
 *  show the currently set value - unlocked, portrait, landscape)
 * @property checkable If this preferences checkbox should be displayed. Use for true/false
 *  preferences
 * @property checked If the checkbox should be set to checked or not checked
 * @property showDivider true if this preference should draw a divider below itself to visually
 *  separate any preferences which follow it.
 */
class CheckPreferenceView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var title: String = ""
    private var desc: String? = null
    private var checkable = false
    private var checked = false
    private var showDivider = true

    private val container: RelativeLayout
    private val titleTextView: TextView
    private val descTextView: TextView
    private val checkbox: ImageButton
    private val divider: View

    // TODO add ability for custom check drawables and avds

    init {
        orientation = VERTICAL

        context.obtainStyledAttributes(attrs, R.styleable.CheckPreferenceView, 0, 0).use { a ->
            if (a.hasValue(R.styleable.CheckPreferenceView_title)) {
                title = a.getString(R.styleable.CheckPreferenceView_title) ?: ""
            }
            if (a.hasValue(R.styleable.CheckPreferenceView_desc)) {
                desc = a.getString(R.styleable.CheckPreferenceView_desc)
            }
            if (a.hasValue(R.styleable.CheckPreferenceView_checkable)) {
                checkable = a.getBoolean(R.styleable.CheckPreferenceView_checkable, false)
            }
            if (a.hasValue(R.styleable.CheckPreferenceView_android_checked)) {
                checked = a.getBoolean(R.styleable.CheckPreferenceView_android_checked, false)
            }
            if (a.hasValue(R.styleable.CheckPreferenceView_showDivider)) {
                showDivider = a.getBoolean(R.styleable.CheckPreferenceView_showDivider, true)
            }
        }

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val innerView = inflater.inflate(R.layout.check_preference_view_layout, this, true)
        titleTextView = innerView.findViewById(R.id.titleTextView)
        descTextView = innerView.findViewById(R.id.descTextView)
        divider = innerView.findViewById(R.id.divider)
        checkbox = innerView.findViewById(R.id.checkbox)
        container = innerView.findViewById(R.id.container)

        setTitle(title)
        setDesc(desc)
        setCheckable(checkable)
        setChecked(checked)
        setShowDivider(showDivider)
    }

    /**
     * Set this preferences titleRes
     */
    fun setTitle(title: String) {
        this.title = title
        titleTextView.text = title
    }

    /**
     * Set this preferences description.
     *
     * @param desc A short sentence describing what this preference does.
     */
    fun setDesc(desc: String?) {
        this.desc = desc
        descTextView.run {
            if (desc != null) {
                text = desc
                visible()
            } else {
                text = ""
                gone()
            }
        }

    }

    /**
     * Set whether or not this preference should display it's right-aligned checkbox
     *
     * @param checkable true if this preference's checkbox should be visible
     */
    fun setCheckable(checkable: Boolean) {
        this.checkable = checkable
        checkbox.run {
            if (checkable) visible() else gone()
        }
    }

    /**
     * Set the state of this preference's checkbox/
     *
     * @param checked true if the checkbox should be marked as checked
     */
    fun setChecked(checked: Boolean) {
        this.checked = checked
        checkbox.run {
            fadeThroughTransition {
                setImageResource(
                    if (checked) {
                        R.drawable.ic_round_check_circle_24px
                    } else {
                        R.drawable.ic_round_check_circle_outline_24px
                    }
                )
            }
        }
    }

    /**
     * Set whether or not a divider (a hairline) should be shown below this preference/
     *
     * @param showDivider true if this preference should show a divider
     */
    fun setShowDivider(showDivider: Boolean) {
        this.showDivider = showDivider
        divider.run {
            if (showDivider) visible() else invisible()
        }
    }

    /**
     * Register a [View.OnClickListener] for this preference. This will be called for any click
     * events on either the preference view as a whole or its checkbox.
     */
    override fun setOnClickListener(l: OnClickListener?) {
        setPreferenceClickListener(l)
    }

    private fun setPreferenceClickListener(listener: OnClickListener?) {
        container.setOnClickListener(listener)
        checkbox.setOnClickListener(listener)
    }
}