package com.words.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.home_fragment.view.*


class HomeFragment: Fragment() {

    companion object {
        const val FRAGMENT_TAG = "home_fragment_tag"
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        //TODO set up views
        view.trendingContainer.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.TRENDING) }
        view.recentContainer.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.RECENT) }
        view.favoriteContainer.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.FAVORITE) }
        view.settings.setOnClickListener { launchSettings() }
        return view
    }

    private fun onMenuButtonCliked(type: ListFragment.ListType) {
        (activity as? MainActivity)?.showListFragment(type)
    }

    private fun launchSettings() {
        (activity as? MainActivity)?.launchSettings()
    }
}