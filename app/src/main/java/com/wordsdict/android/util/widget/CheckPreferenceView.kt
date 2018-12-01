package com.wordsdict.android.util.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.wordsdict.android.R
import com.wordsdict.android.util.gone
import com.wordsdict.android.util.invisible
import com.wordsdict.android.util.visible
import kotlinx.android.synthetic.main.check_preference_view_layout.view.*

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

    //TODO add ability for custom check drawables and avds

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

    fun setTitle(title: String) {
        this.title = title
        titleTextView.text = title
    }

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

    fun setCheckable(checkable: Boolean) {
        this.checkable = checkable
        if (checkable) {
            checkbox.visible()
        } else {
            checkbox.gone()
        }
    }

    fun setChecked(checked: Boolean) {
        this.checked = checked
        if (checked) {
            checkbox.setImageResource(R.drawable.ic_round_check_circle_24px)
        } else {
            checkbox.setImageResource(R.drawable.ic_round_check_circle_outline_24px)
        }
    }

    fun setShowDivider(showDivider: Boolean) {
        this.showDivider = showDivider
        if (showDivider) {
            divider.visible()
        } else {
            divider.invisible()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        setPreferenceClickListener(l)
    }

    private fun setPreferenceClickListener(listener: OnClickListener?) {
        container.setOnClickListener(listener)
    }
}