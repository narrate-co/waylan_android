package com.wordsdict.android.ui.list

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.wordsdict.android.*
import com.wordsdict.android.data.analytics.NavigationMethod
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.ui.common.HeaderBanner
import com.wordsdict.android.util.*
import com.wordsdict.android.util.widget.ElasticAppBarBehavior
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*

/**
 * A flexible Fragment that handles the display of a [ListType]. Each [ListType] configuration is
 * essentially same, differing in only where it's data comes from and what this Fragments
 * AppBarLayout (faked) title is set to.
 *
 */
class ListFragment:
        BaseUserFragment(),
        ListTypeAdapter.ListTypeListener,
        ElasticAppBarBehavior.ElasticViewBehaviorCallback {



    enum class ListType(val fragmentTag: String, val title: String, val homeDestination: Navigator.HomeDestination) {
        TRENDING("trending_fragment_tag", "Trending", Navigator.HomeDestination.TRENDING),
        RECENT("recent_fragment_tag", "Recent", Navigator.HomeDestination.RECENT),
        FAVORITE("favorite_fragment_tag", "Favorite", Navigator.HomeDestination.FAVORITE)
    }

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
            val args = Bundle()
            args.putString("type", type.name)
            listFrag.arguments = args
            return listFrag
        }
    }

    // The MainViewModel used to share data between MainActivity and its child Fragments
    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    // ListFragment's own ViewModel
    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(ListViewModel::class.java)
    }

    // A variable to hold this instances [ListType], retrieved from the Fragment's arguments
    var type: ListType = ListType.TRENDING

    private val adapter by lazy { ListTypeAdapter(this) }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        // Get this instances [ListType]
        type = when (arguments?.getString("type")) {
            ListType.TRENDING.name -> ListType.TRENDING
            ListType.RECENT.name -> ListType.RECENT
            ListType.FAVORITE.name -> ListType.FAVORITE
            else -> ListType.TRENDING
        }

        // Set the toolbarTitle according to the above type
        view.toolbarTitle.text = type.title
        view.toolbarTitleCollapsed.text = type.title
        view.navigationIcon.setOnClickListener {
            // Child fragments of MainActivity should report how the user is navigating away
            // from them. For more info, see [BaseUserFragment.setUnconsumedNavigationMethod]
            setUnconsumedNavigationMethod(NavigationMethod.NAV_ICON)
            activity?.onBackPressed()
        }

        // Add callback to the AppBarLayout's ElasticAppBarBehavior to listen for
        // drag to dismiss events
        ((view.appBar
                .layoutParams as CoordinatorLayout.LayoutParams)
                .behavior as ElasticAppBarBehavior)
                .addCallback(this)

        // Set up fake status bar background to be either transparent or opaque depending on this
        // Fragment's AppBarLayout offset
        setUpStatusBarScrim(view.statusBarScrim, view.appBar)

        // Set up expanding/collapsing "toolbar"
        setUpReachabilityAppBar(view)

        return view
    }

    override fun onEnterTransitionEnded() {
        setUpList()
    }

    private fun setUpList() {
        view?.recyclerView?.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
        )
        view?.recyclerView?.adapter = adapter
        val itemDivider = ListItemDivider(
                ContextCompat.getDrawable(context!!, R.drawable.list_item_divider)
        )
        view?.recyclerView?.addItemDecoration(itemDivider)

        viewModel.getListFilter(type).observe(this, Observer { filter ->
            adapter.submitList(emptyList())
            viewModel.getList(type, filter).observe(this, Observer {
                adapter.submitList(it)
                setBanner((it.isEmpty()))

            })
        })
    }

    /**
     * If the list is empty or the user has not seen the banner (which acts as an onboarding
     * mechanism to describe the contents of this instance's [ListType], set the adapter's
     * header
     */
    private fun setBanner(isListEmpty: Boolean) {
        if (isListEmpty || (!viewModel.getHasSeenBanner(type) && type == ListType.TRENDING)) {
            val text = when (type) {
                ListType.TRENDING -> getString(R.string.list_banner_trending_body)
                ListType.RECENT -> getString(R.string.list_banner_recents_body)
                ListType.FAVORITE -> getString(R.string.list_banner_favorites_body)
            }

            val topButton = if (isListEmpty) getString(R.string.list_banner_get_started_button) else null
            val bottomButton = if (isListEmpty) null else getString(R.string.list_banner_dismiss_button)
            adapter.setHeader(HeaderBanner(text, topButton, bottomButton))
        } else {
            adapter.setHeader(null)
        }
    }

    /**
     * Manually translates views in this Fragment's AppBarLayout to create an expanding/collapsing
     * toolbar.
     */
    private fun setUpReachabilityAppBar(view: View) {

        view.appBar.doOnPreDraw {
            // TODO set height of expanded toolbar based on view height. Set collapsed if
            // TODO under a certain limit

            // set min height
            val minCollapsedHeight = underline.bottom - navigationIcon.top
            val toolbarTitleCollapsedHeight = appBar.toolbarTitleCollapsed.height

            val alphaFraction = 0.6F
            view.appBar.toolbarContainer.minimumHeight = minCollapsedHeight
            view.appBar.addOnOffsetChangedListener(
                    AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                        val totalScrollRange = appBarLayout.totalScrollRange - minCollapsedHeight
                        val interpolationEarlyFinish = Math.abs(verticalOffset.toFloat()) / totalScrollRange

                        // translate the navIcon to make room for the collapsed toolbar title
                        val navIconTransY = (1 - interpolationEarlyFinish) * toolbarTitleCollapsedHeight
                        appBarLayout.navigationIcon.translationY = navIconTransY

                        // hide/show the collapsed toolbar title
                        val offsetInterpolation = getScaleBetweenRange(
                                interpolationEarlyFinish,
                                alphaFraction,
                                1F,
                                0F,
                                1F
                        )
                        appBarLayout.toolbarTitleCollapsed.alpha = offsetInterpolation
                    })
        }

    }

    override fun onWordClicked(word: String) {
        sharedViewModel.setCurrentWord(word)
        (activity as? MainActivity)?.showDetails()
    }

    override fun onBannerClicked(banner: HeaderBanner) {
        //do nothing
    }

    override fun onBannerTopButtonClicked(banner: HeaderBanner) {
        (activity as? MainActivity)?.focusAndOpenSearch()
    }

    override fun onBannerBottomButtonClicked(banner: HeaderBanner) {
        adapter.setHeader(null)
        viewModel.setHasSeenBanner(type, true)
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

        view?.appBar?.translationY = cutDragTo
        view?.recyclerView?.alpha = alpha
        view?.appBar?.alpha = alpha
    }

    override fun onDragDismissed(): Boolean {
        setUnconsumedNavigationMethod(NavigationMethod.DRAG_DISMISS)
        Handler().post { activity?.onBackPressed() }
        return true
    }

}