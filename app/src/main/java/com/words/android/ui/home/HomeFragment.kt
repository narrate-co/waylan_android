package com.words.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.ui.common.BaseUserFragment
import com.words.android.ui.list.ListFragment
import kotlinx.android.synthetic.main.home_fragment.view.*


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
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        view.trendingContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.TRENDING) }
        view.recentContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.RECENT) }
        view.favoriteContainer.setOnClickListener { onMenuButtonClicked(ListFragment.ListType.FAVORITE) }
        view.settings.setOnClickListener { launchSettings() }
        return view
    }

    override fun onEnterTransactionEnded() {
        setUpListPreviews()
    }

    private fun setUpListPreviews() {
        viewModel.getListPreview(ListFragment.ListType.FAVORITE).observe(this, Observer {
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