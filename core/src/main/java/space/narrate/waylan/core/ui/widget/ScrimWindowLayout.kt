package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.content.res.use
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout
import space.narrate.waylan.core.R
import space.narrate.waylan.core.util.getColorStateList
import space.narrate.waylan.core.util.invisible
import space.narrate.waylan.core.util.visible

/**
 * A FrameLayout which adds a status bar scrim to the top of the screen and a background color
 * to a layout.
 *
 * This is helpful when dragging to dismiss a layout. This can be used as the root layout and
 * be excluded from the views being "dragged", causing a scrim to show below the content,
 * helping show the top of the content is separating from the window.
 *
 * If the layout has a collapsing AppBarLayout, the status bar scrim will be invisible if the
 * ABL is fully expanded and turn visible when the ABL begins to scroll.
 */
class ScrimWindowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_ATTR,
    defStyleRes: Int = DEF_STYLE_RES
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val statusBarScrim = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            gravity = Gravity.TOP
        }
    }

    init {
        getContext().theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScrimWindowLayout,
            defStyleAttr,
            defStyleRes
        ).use {
            val backgroundTint = it.getColorStateList(
                context,
                R.styleable.ScrimWindowLayout_backgroundTint,
                android.R.attr.colorBackground
            )

            val statusBarTint = it.getColorStateList(
                context,
                R.styleable.ScrimWindowLayout_statusBarScrimTint,
                android.R.attr.colorBackground
            )

            setBackgroundColor(backgroundTint.defaultColor)
            statusBarScrim.setBackgroundColor(statusBarTint.defaultColor)
        }
        clipToPadding = false
        clipChildren = false
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        statusBarScrim.layoutParams.height = insets.systemWindowInsetTop
        return super.onApplyWindowInsets(insets)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        findAppBarLayout()?.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                if (verticalOffset == 0) statusBarScrim.invisible() else statusBarScrim.visible()
        })

        addView(statusBarScrim, childCount)
    }

    private fun findAppBarLayout(): AppBarLayout? {
        children.forEach {
            if (it is AppBarLayout) return it
        }

        return null
    }

    companion object {
        private val DEF_STYLE_ATTR = R.attr.styleScrimWindow
        private val DEF_STYLE_RES = R.style.Widget_Waylan_ScrimWindow
    }
}