package com.words.android.ui.list

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.words.android.*
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.ElasticAppBarBehavior
import com.words.android.util.displayHeightPx
import com.words.android.util.getColorFromAttr
import com.words.android.util.getScaleBetweenRange
import kotlinx.android.synthetic.main.banner_layout.view.*
import kotlinx.android.synthetic.main.list_fragment.*
import kotlinx.android.synthetic.main.list_fragment.view.*

class ListFragment: BaseUserFragment(), ListTypeAdapter.ListTypeListener, ElasticAppBarBehavior.ElasticViewBehaviorCallback {



    enum class ListType(val fragmentTag: String, val title: String) {
        TRENDING("trending_fragment_tag", "Trending"),
        RECENT("recent_fragment_tag", "Recent"),
        FAVORITE("favorite_fragment_tag", "Favorite")
    }

    companion object {
        fun newTrendingInstance(): ListFragment = newInstance(ListType.TRENDING)
        fun newRecentInstance(): ListFragment = newInstance(ListType.RECENT)
        fun newFavoriteInstance(): ListFragment = newInstance(ListType.FAVORITE)

        private fun newInstance(type: ListType): ListFragment {
            val listFrag = ListFragment()
            val args = Bundle()
            args.putString("type", type.name)
            listFrag.arguments = args
            return listFrag
        }
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(ListViewModel::class.java)
    }

    var type: ListType = ListType.TRENDING

    private val adapter by lazy { ListTypeAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_fragment, container, false)
        type = when (arguments?.getString("type")) {
            ListType.TRENDING.name -> ListType.TRENDING
            ListType.RECENT.name -> ListType.RECENT
            ListType.FAVORITE.name -> ListType.FAVORITE
            else -> ListType.TRENDING
        }
        view.toolbarTitle.text = type.title
        view.toolbarTitleCollapsed.text = type.title
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }

        ((view.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior as ElasticAppBarBehavior).addCallback(this)
        setUpBanner(view, type)

        setUpReachabilityAppBar(view.appBar)

        return view
    }

    override fun onEnterTransactionEnded() {
        setUpList()
    }

    private fun setUpBanner(view: View, type: ListType) {
//        view.bannerLayout.body.text = when (type) {
//            ListType.TRENDING -> getString(R.string.list_banner_trending_body)
//            ListType.RECENT -> getString(R.string.list_banner_recents_body)
//            ListType.FAVORITE -> getString(R.string.list_banner_favorites_body)
//        }
//        view.bannerLayout.topButton.setOnClickListener {
//            viewModel.setHasSeenBanner(type, true)
//        }

//        viewModel.getHasSeenBanner(type).observe(this, Observer {
//            view.bannerLayout.visibility = if (it) View.GONE else View.VISIBLE
//        })
    }

    private fun setUpList() {
        view?.recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view?.recyclerView?.adapter = adapter
        val itemDivider = ListItemDivider(ContextCompat.getDrawable(context!!, R.drawable.list_item_divider))
        view?.recyclerView?.addItemDecoration(itemDivider)

        viewModel.getList(type).observe(this, Observer {
            adapter.submitList(it)
        })
    }

    private fun setUpReachabilityAppBar(appBar: AppBarLayout) {


        appBar.doOnPreDraw {
            val minHeight = appBar.bottom - navigationIcon.top
            val toolbarTitleCollapsedHeight = appBar.toolbarTitleCollapsed.height
            val alphaFraction = 0.6F
            val minAlphaRgb = 255F * 0.94F
            val windowBackgroundColor = activity?.getColorFromAttr(android.R.attr.windowBackground) ?: 0
            appBar.toolbarContainer.minimumHeight = minHeight
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val totalScrollRange = appBarLayout.totalScrollRange - minHeight
                val interpolation = Math.abs(verticalOffset.toFloat()) / appBarLayout.totalScrollRange
                val interpolationEarlyFinish = Math.abs(verticalOffset.toFloat()) / totalScrollRange

                // translate the navIcon to make room for the collapsed toolbar title
                val navIconTransY = (1 - interpolation) * toolbarTitleCollapsedHeight
                appBarLayout.navigationIcon.translationY = navIconTransY

                // hide/show the collapsed toolbar title
                val offsetInterpolation = getScaleBetweenRange(interpolationEarlyFinish, alphaFraction, 1F, 0F, 1F)
                appBarLayout.toolbarTitleCollapsed.alpha = offsetInterpolation

                // set alpha of app bar //TODO create custom ScrollingViewBehavior to support content scrolling under AppBar?
                val backgroundWithAlpha = Color.argb(getScaleBetweenRange(interpolation, 0F, 1F, 255F, minAlphaRgb).toInt(), windowBackgroundColor.red, windowBackgroundColor.green, windowBackgroundColor.blue)
                appBarLayout.setBackgroundColor(backgroundWithAlpha)
            })
        }

    }

    override fun onWordClicked(word: String) {
        sharedViewModel.setCurrentWordId(word)
        (activity as? MainActivity)?.showDetails()
    }

    override fun onDrag(dragFraction: Float, dragTo: Float, rawOffset: Float, rawOffsetPixels: Float, dragDismissScale: Float) {
        val alpha = 1 - dragFraction
        val cutDragTo = dragTo * .15F

        view?.appBar?.translationY = cutDragTo

        view?.recyclerView?.alpha = alpha
        view?.appBar?.alpha = alpha
    }

    override fun onDragDismissed(): Boolean {
        Handler().post { activity?.onBackPressed() }
        return true
    }

}