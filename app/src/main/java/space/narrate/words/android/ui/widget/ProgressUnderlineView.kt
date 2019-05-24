package space.narrate.words.android.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.graphics.withTranslation
import space.narrate.words.android.R
import space.narrate.words.android.util.MathUtils

/**
 * A View that acts as a simple underline when not showing progress and a linear, indeterminate
 * progress bar when showing progress. For both the underline and the progress bar, the view is
 * clipped to a rounded rect.
 *
 * By default, the view draws itself as a static underline. Calling [startProgress] will immediately
 * begin the linear indeterminate progress animation while [stopProgress] will stop it and return
 * to the static underline appearance.
 *
 * TODO add support for custom shape
 * TODO add support for multiple progress indicator colors?
 *
 * XML Properties:
 * @property trackColor The color of the background used when showing the progress bar
 * @property indicatorColor The color of the underline and the progress bars
 */
class ProgressUnderlineView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = DEF_STYLE_RES
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val DEF_STYLE_RES = R.style.Widget_Words_ProgressUnderlineView

        private const val ANIM_DURATION = 2000L
    }

    private var trackPaint: Paint
    private var trackRect: RectF = RectF()

    private var indicatorPaint: Paint

    private var indicatorRect: RectF = RectF()
    private var indicatorWidth = 0F
    private var indicatorSpacing = 0F
    private var minTransX = 0F
    private var maxTransX = 0F

    // A looping value animator. The animatedValue is mapped to translation and scale values
    // used to draw (animate) the two indicator rects
    private val progressAnim = ValueAnimator.ofFloat(-1.0F, 1.0F).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        duration = ANIM_DURATION
        addUpdateListener {
            postInvalidateOnAnimation()
        }
    }

    // State to know whether to draw the underline or the progress bar. Also delegates
    // starting and stopping our progress ValueAnimator
    private var isAnimating = false
        set(value) {
            field = value
            if (value) {
                progressAnim.start()
            } else {
                progressAnim.cancel()
            }
        }

    init {
        val a = getContext().theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressUnderlineView,
                defStyleAttr,
                defStyleRes
        )

        trackPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = a.getColor(R.styleable.ProgressUnderlineView_trackColor, 0)
            style = Paint.Style.FILL
        }
        indicatorPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = a.getColor(R.styleable.ProgressUnderlineView_indicatorColor, 0)
            style = Paint.Style.FILL
        }

        a.recycle()

        clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // The background and underline rect
        trackRect = RectF(0F, 0F, w.toFloat(), h.toFloat())

        // Size variables for our two progress bar rects
        // They are each slightly smaller (8/10ths) of our trackRect
        indicatorWidth = w * 0.8F
        // The distance between the two rects
        indicatorSpacing = w * 0.6F
        // Since they're both the same size, we can use one rect
        indicatorRect = RectF(0F, 0F, indicatorWidth, h.toFloat())

        // The starting point for our translation anim is far left enough to translate both
        // rects far enough to the left so that our first rect's right == our track's left
        minTransX = 0F - (indicatorWidth * 2) - indicatorSpacing
        // The end point is far enough right so that our second rect is far enough right
        // so that it's left == our track's right
        maxTransX = w.toFloat()

        // clip everything we draw to a rounded rect, creating the pill shape
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline) {
                outline.setRoundRect(0, 0, w, h, h / 2F)
            }
        }
    }


    override fun onDraw(canvas: Canvas) {

        // Everything here will be clipped by our outlineProvider

        // Draw the bar's background rect
        canvas.drawRect(trackRect, trackPaint)

        if (isAnimating) {

            // our interpolation from our value animator (-1 to 1)
            val transInterp = progressAnim.animatedValue as Float

            // second rect (shrink)
            // start with the second rect since the first can easily
            // be calculated relative to it
            val rect2TransX = MathUtils.normalize(
                transInterp,
                -1F,
                1F,
                minTransX,
                maxTransX
            )
            val rect2ScaleX = MathUtils.normalize(
                transInterp,
                0.4F,
                1F,
                1F,
                .25F
            )
            val checkpoint = canvas.save()
            // translate and scale for our second rect
            canvas.translate(rect2TransX, 0F)
            canvas.scale(rect2ScaleX, 1F, 1F, 0.5F)
            // draw rect2
            canvas.drawRect(indicatorRect, indicatorPaint)
            //restore
            canvas.restoreToCount(checkpoint)


            // first rect (grow)
            // calculate relative to rect2
            val rect1TransX = rect2TransX + indicatorWidth + indicatorSpacing
            // just translate for our first rect (sliding into the track will appear as if it's
            // growing
            canvas.withTranslation(rect1TransX, 0F) {
                canvas.drawRect(indicatorRect, indicatorPaint)
            }

        } else {
            // Draw the indicator over the track rect for the default, non-animating underline
            canvas.drawRect(trackRect, indicatorPaint)
        }
    }

    /**
     * Show the underline as a linear, indeterminate progress bar.
     */
    fun startProgress() {
        isAnimating = true
    }

    /**
     * Stop the linear, indeterminate progress bar if showing and show the view as a static
     * underline.
     */
    fun stopProgress() {
        isAnimating = false
    }

}