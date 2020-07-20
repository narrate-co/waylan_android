package space.narrate.waylan.core.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import space.narrate.waylan.core.R
import space.narrate.waylan.core.util.MathUtils
import space.narrate.waylan.core.util.isDarkUi
import kotlin.random.Random


class ArtView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes){

  private val isDarkUi = context.isDarkUi

  private val watercolor = BitmapFactory.decodeResource(
    context.resources,
//    R.drawable.watercolor_light
    if (context.isDarkUi) R.drawable.watercolor_dark else R.drawable.watercolor_light
  )
  private val watercolorPaint = Paint().apply {
    isAntiAlias = true
    isFilterBitmap = true
    isDither = true
  }

  private val organicCircleDrawable = ContextCompat.getDrawable(context, R.drawable.ic_organic_circle)

  private val scaleMin = 0.35F
  private val scaleMax = 1.0F

  private var watercolorScale: Float = scaleMin + Random.nextFloat() * (scaleMax - scaleMin)
  private var waterColorScaleX = watercolorScale
  private var waterColorScaleY = watercolorScale

  private val offsetMin = 0.3F
  private val offsetMax = 0.7F

  private var yOffset: Float = offsetMin + Random.nextFloat() * (offsetMax - offsetMin)

  init {
    animateWatercolor()
  }

  private fun updateFilter(
    @FloatRange(
      from = 0.0,
      to = 1.0,
      fromInclusive = true,
      toInclusive = true
    ) brightness: Float = 1.0F,
    @FloatRange(
      from = 0.0,
      to = 1.0,
      fromInclusive = true,
      toInclusive = true
    ) contrast: Float = 1.0F
  ) {
    val b = brightness
    val c = contrast
    val colorMatrix = ColorMatrix(
      floatArrayOf(
        c, 0f, 0f, 0f,
        b, 0f, c, 0f,
        0f, b, 0f, 0f,
        c, 0f, b, 0f,
        0f, 0f, 1f, 0f
      ))
    watercolorPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
  }

  fun animateWatercolor(up: Boolean = true) {
    val startScale = waterColorScaleY
    val endScale = startScale * 1.5F
    val startYOffset = yOffset
    val offsetMultiplier = if (up) -0.2F else 0.1F
    val endYOffset = startYOffset + offsetMultiplier
    ValueAnimator.ofFloat(0F, 1F).apply {
      duration = 10000
      interpolator = PathInterpolator(0F,.93F,0F,1.0F)
      addUpdateListener {
        val progress = it.animatedFraction as Float
        waterColorScaleY = MathUtils.normalize(progress, 0F, 1F, startScale, endScale)
        yOffset = MathUtils.normalize(progress, 0F, 1F, startYOffset, endYOffset)
        postInvalidateOnAnimation()
      }
      start()
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    val cx = w / 2
    val cy = h / 2
    organicCircleDrawable?.setBounds(cx - 100, cy - 100, cx + 100, cy + 100)
  }

  override fun onDraw(canvas: Canvas) {
    drawWatercolor(canvas)
    organicCircleDrawable?.draw(canvas)
  }

  private fun drawWatercolor(canvas: Canvas) {
    // Draw watercolor bitmap
    canvas.withTranslation(
      0F,
      (height.toFloat() - (watercolor.height * waterColorScaleX)) * yOffset
    ) {
      canvas.withScale(waterColorScaleX, waterColorScaleY, pivotY = 1.0F) {
        canvas.drawBitmap(watercolor, 0F, 0F, watercolorPaint)
      }
    }
  }

}