package space.narrate.waylan.core.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
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

  private data class Watercolor(
    @DrawableRes val id: Int,
    val leftAligned: Boolean = true,
    val scaleMin: Float = 0.35F,
    val scaleMax: Float = 1.0F,
    val offsetMin: Float = 0.3F,
    val offsetMax: Float = 0.7F
  ) {

    private var bitmapInstance: Bitmap? = null

    fun bitmap(context: Context): Bitmap {
      if (bitmapInstance == null) {
        bitmapInstance = BitmapFactory.decodeResource(
          context.resources,
          id
        )
      }

      return bitmapInstance!!
    }

    val originalScale: Float = scaleMin + Random.nextFloat() * (scaleMax - scaleMin)
    val maxScaleAdditiveAbs = 0.2F

    var scaleX = originalScale
    var scaleY = originalScale

    val originalYOffset: Float = offsetMin + Random.nextFloat() * (offsetMax - offsetMin)
    val maxYAdditiveAbs = 0.1F

    var yOffset: Float = originalYOffset
  }

  private val lightWatercolorPaintings = listOf(
    Watercolor(
      R.drawable.watercolor_light_l1,
      scaleMin = 0.2F,
      scaleMax = 0.3F
    ),
    Watercolor(
      R.drawable.watercolor_light_l2,
      scaleMin = 0.4F,
      scaleMax = 0.8F
    )
//    Watercolor(
//      R.drawable.watercolor_light_l3,
//      scaleMin = 0.1F,
//      scaleMax = 0.6F
//    )
  )

  private val darkWatercolorPaintings = listOf(
    Watercolor(R.drawable.watercolor_dark)
  )

  private val isDarkUi = context.isDarkUi

  private val watercolor: List<Watercolor> = if (isDarkUi) {
    darkWatercolorPaintings
  } else {
    lightWatercolorPaintings
  }

  private val watercolorPaint = Paint().apply {
    isAntiAlias = true
    isFilterBitmap = true
    isDither = true
  }

  private val organicCircleDrawable = ContextCompat.getDrawable(context, R.drawable.ic_organic_circle)

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

  var smearProgress: Float = 0.0F
    set(value) {
      val up = value < 0F
      val absValue = java.lang.Math.abs(value)
      field = absValue

      for (w in watercolor) {
        val sd = w.maxScaleAdditiveAbs * absValue
        val od = w.maxYAdditiveAbs * value
        w.scaleY = w.originalScale + sd
        w.yOffset = w.originalYOffset + od
      }
      postInvalidateOnAnimation()
    }

  fun animateSmear(up: Boolean = true) {
    ValueAnimator.ofFloat(0F, if (up) -1F else 1F).apply {
      duration = 5000L
      interpolator = DecelerateInterpolator()
      addUpdateListener {
        smearProgress = it.animatedValue as Float
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
    for (w in watercolor) {
      val scaledWidth = w.bitmap(context).width * w.scaleX
      canvas.withTranslation(
        if (w.leftAligned) 0F else (width - scaledWidth),
        (height.toFloat() - (w.bitmap(context).height * w.scaleY)) * w.yOffset
      ) {
        canvas.withScale(w.scaleX, w.scaleY, pivotY = 0.5F, pivotX = 0.25F) {
          canvas.drawBitmap(w.bitmap(context), 0F, 0F, watercolorPaint)
        }
      }
    }
  }
}