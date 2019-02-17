package space.narrate.words.android.util.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import space.narrate.words.android.R
import space.narrate.words.android.util.gone
import space.narrate.words.android.util.invisible
import space.narrate.words.android.util.swapImageResource
import space.narrate.words.android.util.visible
import kotlinx.android.synthetic.main.check_preference_view_layout.view.*

/**
 * A compound ViewGroup which displays an preference item containing a title, a description and
 * an optional checkbox.
 *
 * @property title The main title of the preference.
 * @property desc A short sentence describing what the preference does or the current value of the
 *  preference (ie. a preference with the title Orientation might choose to use the description to
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

    // TODO add ability for custom check drawables and avds

    init {
        orientation = LinearLayout.VERTICAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.CheckPreferenceView, 0, 0)

        if (a != null) {
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

        a?.recycle()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.check_preference_view_layout, this, true)

        setTitle(title)
        setDesc(desc)
        setCheckable(checkable)
        setChecked(checked)
        setShowDivider(showDivider)
    }

    /**
     * Set this preferences title
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
        if (desc != null) {
            descTextView.text = desc
            descTextView.visible()
        } else {
            descTextView.text = ""
            descTextView.gone()
        }
    }

    /**
     * Set whether or not this preference should display it's right-aligned checkbox
     *
     * @param checkable true if this preference's checkbox should be visible
     */
    fun setCheckable(checkable: Boolean) {
        this.checkable = checkable
        if (checkable) {
            checkbox.visible()
        } else {
            checkbox.gone()
        }
    }

    /**
     * Set the state of this preference's checkbox/
     *
     * @param checked true if the checkbox should be marked as checked
     */
    fun setChecked(checked: Boolean) {
        this.checked = checked
        if (checked) {
            checkbox.swapImageResource(R.drawable.ic_round_check_circle_24px)
        } else {
            checkbox.swapImageResource(R.drawable.ic_round_check_circle_outline_24px)
        }
    }

    /**
     * Set whether or not a divider (a hairline) should be shown below this preference/
     *
     * @param showDivider true if this preference should show a divider
     */
    fun setShowDivider(showDivider: Boolean) {
        this.showDivider = showDivider
        if (showDivider) {
            divider.visible()
        } else {
            divider.invisible()
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