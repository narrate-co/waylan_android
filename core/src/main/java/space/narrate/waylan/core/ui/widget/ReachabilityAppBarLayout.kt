package space.narrate.waylan.core.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.use
import androidx.core.view.doOnPreDraw
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import space.narrate.waylan.core.R
import space.narrate.waylan.core.util.MathUtils
import kotlin.math.abs

class ReachabilityAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.reachabilityAppBarLayoutStyle
) : AppBarLayout(context, attrs, defStyleAttr) {

    private var expandedTitle: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var collapsedTitle: TextView
    private val toolbarContainer: ConstraintLayout

    private val elasticBehavior = ElasticAppBarBehavior(context, attrs)

    /**
     * A callback that handles animating views inside this appbar when an ElasticAppBarBehavior
     * is attached as well as provides utility methods to easily listen for drag dismiss events.
     */
    private val elasticCallback = object : ElasticAppBarBehavior.ElasticViewBehaviorCallback {

        val parallaxViews = mutableListOf<View>()
        val alphaViews = mutableListOf<View>()

        var onDismiss: () -> Boolean = { false }

        override fun onDrag(
            dragFraction: Float,
            dragTo: Float,
            rawOffset: Float,
            rawOffsetPixels: Float,
            dragDismissScale: Float
        ) {
            val alpha = 1 - dragFraction
            val cutDragTo = dragTo * .15F

            parallaxViews.forEach { it.translationY = cutDragTo }
            alphaViews.forEach { it.alpha = alpha }
        }

        override fun onDragDismissed(): Boolean {
            return onDismiss()
        }
    }

    /**
     * An offset listener that shows and hides the collapsed toolbar's title when it nears the
     * top of the screen.
     */
    private val appBarOffsetListener = OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val totalOffset = appBarLayout.totalScrollRange.toFloat()
        val currentOffset = abs(verticalOffset).toFloat()
        val collapsedToolbarHeight = toolbar.height

        // When the collapsed toolbar is twice it's height away from the top edge of the screen,
        // start fading in the title until the collapsed toolbar meets the top edge.
        val alpha = MathUtils.normalize(
            currentOffset,
            totalOffset - collapsedToolbarHeight - collapsedToolbarHeight,
            totalOffset - collapsedToolbarHeight,
            0F,
            1F
        )
        collapsedTitle.alpha = alpha
    }

    var title: String = ""
        set(value) {
            if (field != value) {
                field = value
                expandedTitle.text = value
                collapsedTitle.text = value
            }
        }

    private var isDragDismissable: Boolean = true

    init {
        val view = View.inflate(context, R.layout.reachability_app_bar_layout, this)
        expandedTitle = view.findViewById(R.id.toolbar_title)
        toolbar = view.findViewById(R.id.toolbar)
        collapsedTitle = view.findViewById(R.id.toolbar_title_collapsed)
        toolbarContainer = view.findViewById(R.id.toolbar_container)

        // Add the navigation icon to the views to be parallaxed  when ElasticAppBarBehavior
        // is dragging.
        elasticCallback.parallaxViews += toolbar

        elasticBehavior.addCallback(elasticCallback)
        addOnOffsetChangedListener(appBarOffsetListener)

        context.obtainStyledAttributes(
            attrs,
            R.styleable.ReachabilityAppBarLayout,
            defStyleAttr,
            DEF_STYLE_RES
        ).use {
            title = it.getString(R.styleable.ReachabilityAppBarLayout_title) ?: ""
            isDragDismissable = it.getBoolean(R.styleable.ReachabilityAppBarLayout_dragDismissable, true)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setIsDragDismissable(isDragDismissable)
    }

    fun setIsDragDismissable(isDragDismissable: Boolean) {
        if (!isAttachedToWindow) return

        this.isDragDismissable = isDragDismissable
        val params = layoutParams as? CoordinatorLayout.LayoutParams ?: return
        params.behavior = if (isDragDismissable) elasticBehavior else null
        layoutParams = params
    }

    fun doOnElasticDrag(
        parallaxViews: List<View> = emptyList(),
        alphaViews: List<View> = emptyList()
    ) {
        elasticCallback.parallaxViews += parallaxViews
        elasticCallback.alphaViews += alphaViews
    }

    fun doOnElasticDismiss(onDismiss: () -> Boolean) {
        elasticCallback.onDismiss = onDismiss
    }

    fun setOnNavigationIconClicked(onClick: () -> Unit) {
        toolbar.setNavigationOnClickListener { onClick() }
    }

    companion object {
        private val DEF_STYLE_RES = R.style.Widget_Waylan_ReachabilityAppBarLayout_Flat
    }
}

/**
 * A subclass of Toolbar that doesn't "eat" all touch events and allows an ElasticAppBarBehavior to
 * properly consume them.
 */
class ReachabilityToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr) {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}