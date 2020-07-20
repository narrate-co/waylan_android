package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import space.narrate.waylan.core.R
import kotlin.math.min

class LogoView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

  private val semiboldTypeface = ResourcesCompat.getFont(context, R.font.ibm_plex_serif_semibold)

  private val Float.spToDp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)

  private val watercolor = BitmapFactory.decodeResource(context.resources, R.drawable.watercolor_light)
  private val watercolorPaint = Paint().apply {
    style = Paint.Style.FILL
    textAlign = Paint.Align.CENTER
    typeface = semiboldTypeface
    shader = BitmapShader(
      watercolor.scale(
        (watercolor.width * .2).toInt(),
        (watercolor.height * .2).toInt()
      ),
      Shader.TileMode.REPEAT,
      Shader.TileMode.REPEAT
    )
  }
  private val bgPaint = Paint().apply {
    color = 0xD2DBCF
    style = Paint.Style.FILL
    textAlign = Paint.Align.CENTER
    typeface = semiboldTypeface
  }

  private val fgPaint = Paint().apply {
    color = Color.BLACK
    style = Paint.Style.FILL
    textAlign = Paint.Align.RIGHT
    typeface = semiboldTypeface
    textSize = 18F.spToDp
  }

  private var fgText = "Waylan"
  private val fgTextBounds = Rect()

  init {
    fgPaint.getTextBounds(fgText, 0, fgText.length, fgTextBounds)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    watercolorPaint.textSize = min(w, h).toFloat()
    bgPaint.textSize = min(w, h).toFloat()
  }

  override fun onDraw(canvas: Canvas) {
    canvas.drawText("W", width / 2F, height.toFloat(), watercolorPaint)
    canvas.drawText("Waylan", width.toFloat(), height * .8F, fgPaint)
  }
}