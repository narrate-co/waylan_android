package space.narrate.words.android.ui.list

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.*
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.ElasticAppBarBehavior

/**
 * A flexible Fragment that handles the display of a [ListType]. Each [ListType] configuration is
 * essentially same, differing in only where it's data comes from and what this Fragments
 * AppBarLayout (faked) title is set to.
 *
 */
class ListFragment:
    BaseUserFragment(),
    ListItemAdapter.ListItemListener,
    ElasticAppBarBehavior.ElasticViewBehaviorCallback {


    companion object {
        /**
         * Static helper method to construct a [ListFragment] which displays a list of all
         * trending words on Words.
         */
        fun newTrendingInstance(): ListFragment = newInstance(ListType.TRENDING)

        /**
         * Static helper method to construct a [ListFragment] which displays a list of all
         * words the current user has recently viewed.
         */
        fun newRecentInstance(): ListFragment = newInstance(ListType.RECENT)

        /**
         * Static helper method to construct a [ListFragment] which displays a list of all words
         * the current user has marked as favorite.
         */
        fun newFavoriteInstance(): ListFragment = newInstance(ListType.FAVORITE)

        private fun newInstance(type: ListType): ListFragment {
            val listFrag = ListFragment()
            listFrag.listType = type
            return listFrag
        }
    }

    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var appBar: AppBarLayout
    private lateinit var statusBarScrim: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbarContainer: ConstraintLayout
    private lateinit var toolbarTitle: AppCompatTextView
    private lateinit var toolbarTitleCollapsed: AppCompatTextView
    private lateinit var underline: View

    // The MainViewModel used to share data between MainActivity and its child Fragments
    private val sharedViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)
            .get(MainViewModel::class.java)
    }

    // ListFragment's own ViewModel
    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(ListViewModel::class.java).apply {
                setListType(this@ListFragment.listType)
            }
    }

    // A variable to hold this instances [ListType], retrieved from the Fragment's arguments
    var listType: ListType = ListType.TRENDING

    private val adapter by lazy { ListItemAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        appBar = view.findViewById(R.id.app_bar)
        statusBarScrim = view.findViewById(R.id.status_bar_scrim)
        recyclerView = view.findViewById(R.id.recycler_view)
        toolbarContainer = view.findViewById(R.id.toolbar_container)
        toolbarTitle = view.findViewById(R.id.toolbar_title)
        toolbarTitleCollapsed = view.findViewById(R.id.toolbar_title_collapsed)
        underline = view.findViewById(R.id.underline)

        // Set the toolbarTitle according to the above listType
        navigationIcon.setOnClickListener {
            // Child fragments of MainActivity should report how the user is navigating away
            // from them. For more info, see [BaseUserFragment.setUnconsumedNavigationMethod]
            setUnconsumedNavigationMethod(NavigationMethod.NAV_ICON)
            activity?.onBackPressed()
        }

        // Add callback to the AppBarLayout's ElasticAppBarBehavior to listen for
        // drag to dismiss events
        ((appBar
            .layoutParams as CoordinatorLayout.LayoutParams)
            .behavior as ElasticAppBarBehavior)
            .addCallback(this)

        // Set up fake status bar background to be either transparent or opaque depending on this
        // Fragment's AppBarLayout offset
        setUpStatusBarScrim(statusBarScrim, appBar)

        // Set up expanding/collapsing "toolbar"
        setUpReachabilityAppBar()

        setUpList()
    }

    private fun setUpList() {
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        recyclerView.adapter = adapter
        val itemDivider = ListItemDividerDecoration(
            ContextCompat.getDrawable(context!!, R.drawable.list_item_divider)
        )
        recyclerView.addItemDecoration(itemDivider)

        viewModel.listType.observe(this, Observer {
            toolbarTitle.text = it.title
            toolbarTitleCollapsed.text = it.title
        })

        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    /**
     * If the list is empty or the user has not seen the banner (which acts as an onboarding
     * mechanism to describe the contents of this instance's [ListType], set the adapter's
     * header
     */


    /**
     * Manually translates views in this Fragment's AppBarLayout to create an expanding/collapsing
     * toolbar.
     */
    private fun setUpReachabilityAppBar() {

        appBar.doOnPreDraw {
            // TODO set height of expanded toolbar based on view height. Set collapsed if
            // TODO under a certain limit

            // set min height
            val minCollapsedHeight = underline.bottom - navigationIcon.top
            val toolbarTitleCollapsedHeight = toolbarTitleCollapsed.height

            val alphaFraction = 0.6F
            toolbarContainer.minimumHeight = minCollapsedHeight
            appBar.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    val totalScrollRange = appBarLayout.totalScrollRange - minCollapsedHeight
                    val interpolationEarlyFinish = Math.abs(
                        verticalOffset.toFloat()
                    ) / totalScrollRange

                    // translate the navIcon to make room for the collapsed toolbar title
                    val navIconTransY =
                        (1 - interpolationEarlyFinish) * toolbarTitleCollapsedHeight
                    navigationIcon.translationY = navIconTransY

                    // hide/show the collapsed toolbar title
                    val offsetInterpolation = MathUtils.normalize(
                        interpolationEarlyFinish,
                        alphaFraction,
                        1F,
                        0F,
                        1F
                    )
                    toolbarTitleCollapsed.alpha = offsetInterpolation
                })
        }

    }

    override fun onWordClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
        (requireActivity() as MainActivity).showDetails()
    }

    override fun onBannerClicked() {
        // do nothing
    }

    override fun onBannerLabelClicked() {
        // do nothing
    }

    override fun onBannerTopButtonClicked() {
        sharedViewModel.onShouldOpenAndFocusSearch()
    }

    override fun onBannerBottomButtonClicked() {
        viewModel.onBannerDismissClicked()
    }

    override fun onDrag(
        dragFraction: Float,
        dragTo: Float,
        rawOffset: Float,
        rawOffsetPixels: Float,
        dragDismissScale: Float
    ) {
        // Translate individual views to create a parallax effect
        val alpha = 1 - dragFraction
        val cutDragTo = dragTo * .15F

        appBar.translationY = cutDragTo
        recyclerView.alpha = alpha
        appBar.alpha = alpha
    }

    override fun onDragDismissed(): Boolean {
        setUnconsumedNavigationMethod(NavigationMethod.DRAG_DISMISS)
        Handler().post { activity?.onBackPressed() }
        return true
    }

}