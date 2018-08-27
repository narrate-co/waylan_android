package com.words.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.words.android.MainActivity
import com.words.android.R
import com.words.android.ui.list.ListFragment
import kotlinx.android.synthetic.main.home_fragment.view.*


class HomeFragment: Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        //TODO set up views
        view.trending.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.TRENDING) }
        view.recent.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.RECENT) }
        view.favorite.setOnClickListener { onMenuButtonCliked(ListFragment.ListType.FAVORITE) }
        return view
    }

    private fun onMenuButtonCliked(type: ListFragment.ListType) {
        (activity as? MainActivity)?.showListFragment(type)
    }
}