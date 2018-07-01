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
import com.words.android.data.repository.Word
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
        dashboardViewModel.favoriteWord.observe(this, Observer {
            it?.firstOrNull()?.let {
                popularCard.setWord(it)
            }
        })
        return view
    }

    override fun onStop() {
        view?.popularCard?.removePopularCardListener()
        super.onStop()
    }

    override fun onPopularCardClicked(word: Word) {
        println("onPopularCardClicked. id = ${word.userWord?.id}")
        val id = word.userWord?.id ?: return
        sharedViewModel.setCurrentWordId(id)
        (activity as MainActivity).showDetails()
    }

    override fun onPopularSynonymClicked(id: String) {
        sharedViewModel.setCurrentWordId(id)
        (activity as MainActivity).showDetails()
    }
}

