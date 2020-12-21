package space.narrate.waylan.android.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.core.view.get
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
import space.narrate.waylan.android.R
import space.narrate.waylan.core.util.themeColor

private const val SELECTION_ANIMATION_DURATION = 150L
private const val HIDE_TRANSLATION_Y = 100F

class FloatingNavigationBar @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

  interface SelectionCallback {
    fun onSelectionChanged(itemId: Int, index: Int)
  }

  private val backgroundShapeDrawable: MaterialShapeDrawable
  private val selectionShapeDrawable: MaterialShapeDrawable

  private var menu: MenuBuilder? = null

  private var callback: SelectionCallback? = null

  private var selectedIndex = 0
    set(value) {
      // TODO: Maybe exit early when the field == value and this isn't the initial ping
      if (value !in 0 until childCount) return

      field = value
      callback?.onSelectionChanged(menu?.get(value)?.itemId ?: -1, value)
      children.forEachIndexed { i, v ->
        if (v is TextView) {
          if (i == value) {
            v.setTextColor(v.context.themeColor(R.attr.colorOnSecondary))
            // Update the location of the selection shape
            maybeAnimateSelectionIndicatorToTarget(v)
          } else {
            v.setTextColor(v.context.getColorStateList(R.color.color_on_surface_secondary_medium))
          }
        }
      }
      postInvalidate()
    }

  private val selectionIndicatorBounds: Rect = Rect()

  private var selectionIndicatorAnimator: ValueAnimator? = null

  private var showHideAnimator: ValueAnimator? = null
  var showHideProgress: Float = 1F
    set(value) {
      if (field == value) return
      field = value

      translationY = HIDE_TRANSLATION_Y * (1F - value)
      alpha = value
      postInvalidateOnAnimation()
    }

  init {
    orientation = HORIZONTAL

    backgroundShapeDrawable = MaterialShapeDrawable(
      context,
      attrs,
      R.attr.chipStyle,
      0
    ).apply {
      fillColor = ColorStateList.valueOf(context.themeColor(R.attr.colorSurfaceSecondary))
      // Set the alpha of the entire background to 93%
      alpha = (255 * .93).toInt()
    }
    background = backgroundShapeDrawable

    selectionShapeDrawable = MaterialShapeDrawable(
      context,
      attrs,
      R.attr.chipStyle,
      0
    ).apply {
      fillColor = ColorStateList.valueOf(context.themeColor(R.attr.colorSecondary))
    }

    context.obtainStyledAttributes(
      attrs,
      R.styleable.FloatingNavigationBar,
      defStyleAttr,
      defStyleRes
    ).use {
      val menu = it.getResourceId(R.styleable.FloatingNavigationBar_menu, 0)
      if (menu != 0) {
        setMenu(menu)
      }
    }
  }

  fun setSelectionCallback(callback: SelectionCallback?) {
    this.callback = callback
  }

  fun show() = showHide(true)

  fun hide() = showHide(false)

  private fun showHide(show: Boolean) {
    val from = showHideProgress
    val to = if (show) 1F else 0F
    if (showHideProgress == to && showHideAnimator != null) return

    showHideAnimator?.cancel()
    showHideAnimator = null

    showHideAnimator = ValueAnimator.ofFloat(from, to).apply {
      duration = SELECTION_ANIMATION_DURATION
      interpolator = if (to == 0F) AccelerateInterpolator() else DecelerateInterpolator()
      addUpdateListener {
        showHideProgress = it.animatedValue as Float
      }
      start()
    }
  }

  private fun setMenu(@MenuRes menuRes: Int) {
    removeAllViews()

    menu = MenuBuilder(context)
    MenuInflater(context).inflate(menuRes, menu)
    menu!!.visibleItems.forEachIndexed { index, item ->
      val tv = LayoutInflater.from(context).inflate(R.layout.floating_navigation_bar_item, this, false) as TextView
      tv.text = item.title
      tv.setOnClickListener {
        selectedIndex = index
      }
      this@FloatingNavigationBar.addView(tv)
    }
  }

  private fun maybeAnimateSelectionIndicatorToTarget(target: View) {
    val currentFraction = selectionIndicatorAnimator?.animatedFraction ?: 1F
    selectionIndicatorAnimator?.cancel()
    selectionIndicatorAnimator = null

    val targetBounds = Rect(
      target.left,
      target.top,
      target.right,
      target.bottom
    )
    if (selectionIndicatorBounds.width() == 0) {
      // If there currently is no width to the indicator, directly set its bounds
      // and draw it to the screen. There is no tween to be done.
      selectionIndicatorBounds.set(targetBounds)
      postInvalidate()
    } else {
      val currentBounds = selectionIndicatorBounds
      selectionIndicatorAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
        duration = (currentFraction * SELECTION_ANIMATION_DURATION).toLong()
        interpolator = DecelerateInterpolator()
        addUpdateListener {
          val progress = it.animatedValue as Float
          selectionIndicatorBounds.set(
            (currentBounds.left + ((targetBounds.left - currentBounds.left) * progress)).toInt(),
            (currentBounds.top + ((targetBounds.top - currentBounds.top) * progress)).toInt(),
            (currentBounds.right + ((targetBounds.right - currentBounds.right) * progress)).toInt(),
            (currentBounds.bottom + ((targetBounds.bottom - currentBounds.bottom) * progress)).toInt()
          )
          postInvalidateOnAnimation()
        }
        start()
      }
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (childCount == 0) return
    // If the bounds haven't been set, ping the selected index to calculate the initial bounds.
    if (selectionIndicatorBounds.isEmpty) selectedIndex = selectedIndex

    selectionShapeDrawable.setBounds(
      selectionIndicatorBounds.left,
      selectionIndicatorBounds.top,
      selectionIndicatorBounds.right,
      selectionIndicatorBounds.bottom
    )
    selectionShapeDrawable.draw(canvas)
  }

}