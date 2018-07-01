package com.words.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.words.android.App
import com.words.android.MainActivity
import com.words.android.MainViewModel
import com.words.android.R
import kotlinx.android.synthetic.main.dashboard_fragment.*
import kotlinx.android.synthetic.main.dashboard_fragment.view.*

class DashboardFragment: Fragment(), PopularCardView.PopularCardListener {

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private val dashboardViewModel by lazy {
        ViewModelProviders
                .of(this, (activity?.application as App).viewModelFactory)
                .get(DashboardViewModel::class.java)
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(activity!!, (activity!!.application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dashboard_fragment, container, false)
        view.popularCard.setPopularCardListener(this)
        view.recentsCard.setCardIcon(R.drawable.ic_access_time_black_24dp)
        view.recentsCard.setListCardListener(recentsCardListener)
        view.favoritesCard.setListCardListener(favoritesCardListener)
        view.favoritesCard.setCardIcon(R.drawable.ic_favorite_black_24dp)
        dashboardViewModel.favoriteWord.observe(this, Observer {
            it?.let { favoritesCard.setWords(it) }
            it?.firstOrNull()?.let {
                popularCard.setWord(it)
            }
        })
        dashboardViewModel.recentWords.observe(this, Observer {
            it?.let { recentsCard.setWords(it) }
        })
        return view
    }

    override fun onPopularCardClicked(id: String) {
        sharedViewModel.setCurrentWordId(id)
        (activity as MainActivity).showDetails()
    }

    override fun onPopularSynonymClicked(id: String) {
        sharedViewModel.setCurrentWordId(id)
        (activity as MainActivity).showDetails()
    }

    val recentsCardListener = object : ListCardLayout.ListCardListener {
        override fun onListWordClicked(id: String) {
            sharedViewModel.setCurrentWordId(id)
            (activity as MainActivity).showDetails()
        }
        override fun onListMoreClicked() {
            //TODO show full list of recents
        }
    }

    val favoritesCardListener = object : ListCardLayout.ListCardListener {
        override fun onListWordClicked(id: String) {
            sharedViewModel.setCurrentWordId(id)
            (activity as MainActivity).showDetails()
        }
        override fun onListMoreClicked() {
            //TODO show full list of favorites
        }
    }

}

