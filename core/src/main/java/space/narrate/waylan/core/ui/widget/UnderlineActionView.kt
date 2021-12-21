package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.use
import space.narrate.waylan.core.R
import space.narrate.waylan.core.databinding.UnderlineActionViewLayoutBinding
import space.narrate.waylan.core.util.getColorStateList
import space.narrate.waylan.core.util.invisible
import space.narrate.waylan.core.util.visible

private val DEF_STYLE_ATTR = R.attr.underlineActionViewStyle
private val DEF_STYLE_RES = R.style.Widget_Waylan_UnderlineActionView

/**
 * A composite view which shows a play/stop button above a [ProgressUnderlineView].
 */
class UnderlineActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_ATTR,
    defStyleRes: Int = DEF_STYLE_RES
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: UnderlineActionViewLayoutBinding

    init {
        orientation = VERTICAL
        binding = UnderlineActionViewLayoutBinding.inflate(LayoutInflater.from(context), this)

        // Get attributes and set them
        getContext().obtainStyledAttributes(
            attrs,
            R.styleable.UnderlineActionView,
            defStyleAttr,
            defStyleRes
        ).use {
            val actionIcon = it.getResourceId(R.styleable.UnderlineActionView_actionIcon, 0)
            setActionIconResource(actionIcon)

            if (it.hasValue(R.styleable.UnderlineActionView_actionIconTint)) {
                val tint = it.getColorStateList(
                    context,
                    R.styleable.UnderlineActionView_actionIconTint,
                    R.attr.colorOnSurface
                )
                setActionIconTint(tint)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1F else 0.38F
    }

    fun setActionIconResource(resId: Int) {
        if (resId == 0) {
            invisible()
        } else {
            visible()
        }
        binding.imageView.setImageResource(resId)
    }

    fun setActionIconTint(color: ColorStateList) {
        binding.imageView.imageTintList = color
    }

    fun isLoading(): Boolean = binding.underline.isStarted()

    fun setLoading(loading: Boolean) {
        if (loading) {
            binding.underline.startProgress()
        } else {
            binding.underline.stopProgress()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
      binding.imageView.setOnClickListener(l)
    }
}