package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import space.narrate.waylan.core.util.themeDimensionPixelSize
import kotlin.math.cos
import kotlin.math.sin

class OrganicView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

  private var path = Path()
  private var paint = Paint().apply {
    style = Paint.Style.STROKE
    strokeWidth = 10F
    color = Color.BLACK
    isAntiAlias = true
  }

  data class OrganicPointF(
    val center: PointF,
    val radius: Float = 100F
  ) {

    val topHandle: PointF = PointF(x(radius, 0F), y(radius, 0F))
    val rightHandle: PointF = PointF(x(radius, 90F), y(radius, 90F))
    val bottomHandle: PointF = PointF(x(radius, 180F), y(radius, 180F))
    val leftHandle: PointF = PointF(x(radius, 270F), y(radius, 270F))

    private fun x(r: Float, ro: Float): Float {
      return r * sin(ro) + center.x;
    }

    private fun y(r: Float, ro: Float): Float {
      return ((r * cos(ro)) * -1F) + center.y
    }

    private fun oppositeRotation(rotation: Float): Float {
      if (rotation >= 180F) return rotation - 180F
      return rotation + 180F
    }
  }


  private fun createCircle() {
    val topPoint = PointF(width / 2F, 270F)
    val top = OrganicPointF(topPoint)
    path.moveTo(top.center.x,top.center.y)
//
    val rightPoint = PointF(width.toFloat(), height / 2F)
    val right = OrganicPointF(rightPoint)
    path.cubicTo(
      top.rightHandle.x, top.rightHandle.y,
      right.topHandle.x, right.topHandle.y,
      right.center.x, right.center.y
    )

    val bottomPoint = PointF(width / 2F, height.toFloat())
    val bottom = OrganicPointF(bottomPoint)
    path.lineTo(bottomPoint.x, bottomPoint.y)

    val leftPoint = PointF(0F, height / 2F)
    val left = OrganicPointF(leftPoint)
    path.lineTo(leftPoint.x, leftPoint.y)

    path.close()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    createCircle()
  }

  override fun onDraw(canvas: Canvas?) {
    canvas?.drawPath(path, paint)
  }
}

