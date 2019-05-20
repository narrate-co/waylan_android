package space.narrate.words.android.util.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.R
import space.narrate.words.android.util.children
import space.narrate.words.android.util.getColorFromAttr
import space.narrate.words.android.util.invisible
import space.narrate.words.android.util.visible

/**
 * A FrameLayout which adds a status bar scrim to the top of the screen.
 *
 * If the layout has a collapsing AppBarLayout, the status bar scrim will be invisible if the
 * ABL is fully expanded and turn visible when the ABL begins to scroll.
 */
class ScrimmedFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val statusBarScrim = View(context).apply {
        setBackgroundColor(context.getColorFromAttr(android.R.attr.colorBackground))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            gravity = Gravity.TOP
        }
    }

    init {
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
}