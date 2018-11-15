package com.words.android.util

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class InsetFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (fitsSystemWindows) {
            var consumed = false
            children.forEach {
                it.dispatchApplyWindowInsets(insets)
                if (insets.isConsumed) {
                    consumed = true
                }
            }
            return if (consumed) insets.consumeSystemWindowInsets() else insets
        } else {
            return super.onApplyWindowInsets(insets)
        }
    }
}