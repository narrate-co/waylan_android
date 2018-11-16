package com.words.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.ui.common.BaseUserFragment
import com.words.android.ui.list.ListFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment: BaseUserFragment() {

    companion object {
        const val FRAGMENT_TAG = "home_fragment_tag"
        fun newInstance() = HomeFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(HomeViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        view.trendingContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.TRENDING) }
        view.recentContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.RECENT) }
        view.favoriteContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.FAVORITE) }
        view.settings.setOnClickListener { launchSettings() }
        setUpStatusBarScrim(view.statusBarScrim)
        setUpReachabilityParams(view)
        return view
    }

    override fun onEnterTransactionEnded() {
        setUpListPreviews()
    }

    private fun setUpReachabilityParams(view: View) {
        view.doOnPreDraw {
            val totalHeight = view.scrollContainer.height
            val menuContainerHeight = view.constraintContainer.height
            val topOffset = Math.max(view.statusBarScrim.height, totalHeight - menuContainerHeight - resources.getDimensionPixelSize(R.dimen.search_min_peek_height) - resources.getDimensionPixelSize(R.dimen.home_menu_bottom_offset_min))
            view.scrollContainer.updatePadding(top = topOffset)
        }
    }

    private fun setUpListPreviews() {
        viewModel.getListPreview(ListFragment.ListType.TRENDING).observe(this, Observer {
            view?.trendingPreview?.text = it
        })
        viewModel.getListPreview(ListFragment.ListType.RECENT).observe(this, Observer {
            view?.recentPreview?.text = it
        })
        viewModel.getListPreview(ListFragment.ListType.FAVORITE).observe(this, Observer {
            view?.favoritePreview?.text = it
        })
    }

    private fun onMenuButtonClicked(type: ListFragment.ListType) {
        (activity as? MainActivity)?.showListFragment(type)
    }

    private fun launchSettings() {
        (activity as? MainActivity)?.launchSettings()
    }
}