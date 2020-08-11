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
import android.view.animation.PathInterpolator
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
    val totalScaleAdditive = 0.5F
    val currentScaleAdditive = 0F

    var scaleX = originalScale
    var scaleY = originalScale

    val originalYOffset: Float = offsetMin + Random.nextFloat() * (offsetMax - offsetMin)
    val totalYAdditive = -0.3F
    val currentYAdditive = 0.0F

    var yOffset: Float = originalYOffset
  }

  private val lightWatercolorPaintings = listOf(
    Watercolor(R.drawable.watercolor_light),
    Watercolor(
      R.drawable.watercolor_light_2,
      leftAligned = false,
      scaleMin = 0.2F,
      scaleMax = 0.7F
    )
//    Watercolor(R.drawable.watercolor_light_l1),
//    Watercolor(R.drawable.watercolor_light_l2),
//    Watercolor(R.drawable.watercolor_light_l3),
  )

  private val darkWatercolorPaintings = listOf(
    Watercolor(R.drawable.watercolor_dark)
  )

  private val isDarkUi = context.isDarkUi

  private val watercolor = if (isDarkUi) darkWatercolorPaintings[0] else lightWatercolorPaintings[0]

  private val watercolorPaint = Paint().apply {
    isAntiAlias = true
    isFilterBitmap = true
    isDither = true
  }

  private val organicCircleDrawable = ContextCompat.getDrawable(context, R.drawable.ic_organic_circle)

  init {
    animateSmear()
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

  var smearProgress: Float = 0.0F
    set(value) {
      println("smearProgress - $value")
      field = value
      val totalScaleDelta = 0.2F
      val sd = totalScaleDelta * value
      watercolor.scaleY = watercolor.originalScale + sd
      postInvalidateOnAnimation()
    }

  fun animateSmear(up: Boolean = true) {
    println("animateSmear::up: $up")
    SpringAnimation(this, object : FloatPropertyCompat<ArtView>("artView") {
      override fun getValue(artView: ArtView?): Float {
        return artView?.smearProgress ?: 0F
      }

      override fun setValue(artView: ArtView?, value: Float) {
        println("animateSmear::setValue - ${value / 10000F}")
        artView?.smearProgress = value / 10000F
      }
    }).apply {
      val spring = SpringForce()
      spring.stiffness = 5F
      spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
      setSpring(spring)
      setStartValue((if (up) 1F else 0F) * 10000F)
      animateToFinalPosition((if (up) 0.0F else 1F) * 10000F)
    }
  }

  fun smear(
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = true, toInclusive = true) progress: Float,
    up: Boolean = true
  ) {

    val scaleAdditive = MathUtils.normalize(progress, 0F, 1F, watercolor.currentScaleAdditive, watercolor.totalScaleAdditive)
    watercolor.scaleY = watercolor.originalScale + scaleAdditive

    val yAdditive = MathUtils.normalize(progress, 0F, 1F, watercolor.currentYAdditive, watercolor.totalYAdditive)
    watercolor.yOffset = watercolor.originalYOffset + yAdditive

    postInvalidateOnAnimation()
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
    val scaledWidth = watercolor.bitmap(context).width * watercolor.scaleX

    canvas.withTranslation(
      if (watercolor.leftAligned) 0F else (width - scaledWidth),
      (height.toFloat() - (watercolor.bitmap(context).height * watercolor.scaleX)) * watercolor.yOffset
    ) {
      canvas.withScale(watercolor.scaleX, watercolor.scaleY, pivotY = 0.5F, pivotX = 0.25F) {
        canvas.drawBitmap(watercolor.bitmap(context), 0F, 0F, watercolorPaint)
      }
    }
  }

}