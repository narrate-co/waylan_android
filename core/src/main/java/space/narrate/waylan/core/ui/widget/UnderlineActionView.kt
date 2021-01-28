package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.use
import space.narrate.waylan.core.R
import space.narrate.waylan.core.databinding.UnderlineActionViewLayoutBinding
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.invisible
import space.narrate.waylan.core.util.visible

/**
 * A composite view which shows a play/stop button above a [ProgressUnderlineView].
 */
class UnderlineActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
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