package space.narrate.waylan.core.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.use
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.MaterialToolbar
import java.util.*
import kotlin.math.abs
import space.narrate.waylan.core.R
import space.narrate.waylan.core.util.MathUtils

/**
 * A large app bar that contains an expanded title and is able to collapse into a normal size
 * toolbar. By default, this class also handles configuring an elastic behavior on this app bar to
 * allow clients to drag to dismiss it.
 */
class ReachabilityAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.reachabilityAppBarLayoutStyle
) : AppBarLayout(context, attrs, defStyleAttr) {

    /**
     * Enable an app with multiple screens, each with their own app bar, to share the
     * expanded/collapsed state of their app bars. This makes it such that moving between screens
     * doesn't require you to always scroll up the app bar or always scroll down an app bar if
     * before dragging to go back.
     *
     * This interface should be implemented by an object which is shared between instances
     * of app bars.
     */
    interface ReachableContinuityNavigator {
        /**
         * The state of this app bar.
         *
         * [id] is used to uniquely identify an instance of an app bar to avoid broadcasting its
         * state and having it applied to itself. [expanded] is whether or not it is expanded or
         * collapsed.
         */
        data class State(val id: String, val expanded: Boolean)
        val reachabilityState: LiveData<State>
        fun setReachabilityState(state: State)
    }

    private var expandedTitle: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var collapsedTitle: TextView
    private val toolbarContainer: ConstraintLayout

    // A unique identifier for this instance of the app bar.
    private val id = UUID.randomUUID().toString()

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

    private var reachableContinuityNavigator: ReachableContinuityNavigator? = null

    private var currentlyExpanded = false

    private var currentOffsetPercentage = 0.0F
        set(value) {
            field = value
            val expanded = value <= 0.5F
            if (expanded != currentlyExpanded) {
                currentlyExpanded = expanded
                reachableContinuityNavigator?.setReachabilityState(
                    ReachableContinuityNavigator.State(id, expanded)
                )
            }
        }

    /**
     * An offset listener that shows and hides the collapsed toolbar's title when it nears the
     * top of the screen. This listener also keeps track of the current offset and updates
     * [currentOffsetPercentage] and [isExpanded].
     */
    private val appBarOffsetListener = OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val totalOffset = appBarLayout.totalScrollRange.toFloat()
        val currentOffset = abs(verticalOffset).toFloat()
        val collapsedToolbarHeight = toolbar.height
        currentOffsetPercentage = currentOffset / totalOffset

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

    @StyleRes
    var expandedTitleTextAppearance: Int = R.style.TextAppearance_MaterialComponents_Headline3
        set(value) {
            field = value
            expandedTitle.setTextAppearance(value)
        }

    var expandedTitleVerticalOffset: Float = 0.6F
        set(value) {
            field = value
            val params = (expandedTitle.layoutParams as ConstraintLayout.LayoutParams)
            params.verticalBias = value
            expandedTitle.layoutParams = params
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
            toolbarContainer.layoutParams.height = it.getDimensionPixelSize(
                R.styleable.ReachabilityAppBarLayout_expandedHeight,
                toolbarContainer.height
            )
            title = it.getString(R.styleable.ReachabilityAppBarLayout_title) ?: ""
            isDragDismissable = it.getBoolean(R.styleable.ReachabilityAppBarLayout_dragDismissable, true)
            expandedTitleTextAppearance = it.getResourceId(
                R.styleable.ReachabilityAppBarLayout_expandedTitleTextAppearance,
                expandedTitleTextAppearance
            )
            expandedTitleVerticalOffset = it.getFloat(
                R.styleable.ReachabilityAppBarLayout_expandedTitleVerticalBias,
                expandedTitleVerticalOffset
            )
            val navigationIcon = it.getResourceId(
                R.styleable.ReachabilityAppBarLayout_navigationIcon,
                0
            )
            if (navigationIcon != 0) {
                toolbar.setNavigationIcon(navigationIcon)
            } else {
                toolbar.setNavigationIcon(null)
            }
            val menu = it.getResourceId(R.styleable.ReachabilityAppBarLayout_menu, 0)
            if (menu != 0) {
                setMenu(menu)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setIsDragDismissable(isDragDismissable)
    }

    /**
     * A convenience method to allow lifecycleOwners which use a ReachabilityAppBar to setup
     * and react to changes in other ReachabilityAppBars.
     */
    fun setReachableContinuityNavigator(
        lifecycleOwner: LifecycleOwner,
        navigator: ReachableContinuityNavigator
    ) {
        this.reachableContinuityNavigator = navigator
        navigator.reachabilityState.observe(lifecycleOwner) { state ->
            if (state.id != id) setExpanded(state.expanded, false)
        }
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

    fun setMenu(@MenuRes menuRes: Int) {
        toolbar.inflateMenu(menuRes)
    }

    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        toolbar.setOnMenuItemClickListener(listener)
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