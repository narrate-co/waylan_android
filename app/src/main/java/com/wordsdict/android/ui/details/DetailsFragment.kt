package com.wordsdict.android.ui.details

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.wordsdict.android.MainViewModel
import com.wordsdict.android.R
import com.wordsdict.android.data.analytics.NavigationMethod
import com.wordsdict.android.data.repository.MerriamWebsterSource
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.*
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*

class DetailsFragment: BaseUserFragment(), DetailsAdapter.Listener, ElasticAppBarBehavior.ElasticViewBehaviorCallback {


    companion object {
        private val TAG = DetailsFragment::class.java.simpleName
        const val FRAGMENT_TAG = "details_fragment_tag"
        fun newInstance() = DetailsFragment()
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val detailsViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(DetailsViewModel::class.java)
    }

    private val adapter: DetailsAdapter = DetailsAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        ((view.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior as ElasticAppBarBehavior).addCallback(this)
        view.navigationIcon.setOnClickListener {
            setUnconsumedNavigationMethod(NavigationMethod.NAV_ICON)
            activity?.onBackPressed()
        }

        setUpStatusBarScrim(view.statusBarScrim, view.appBar)

        return view
    }

    //defer load intensive work until after transition has ended
    override fun onEnterTransitionEnded() {
        setUpRecyclerView()

        sharedViewModel.currentWord.observe(this, Observer {
            detailsViewModel.setWordId(it)
            sharedViewModel.setCurrentWordRecented()
        })

        detailsViewModel.wordPropertiesSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })

        detailsViewModel.wordsetSource.observe(this, Observer {
            if (it != null) {
                adapter.submitWordSource(it)
            }
        })
        detailsViewModel.firestoreUserSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
        detailsViewModel.firestoreGlobalSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
        detailsViewModel.merriamWebsterSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
    }


    private fun setUpRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView?.adapter = adapter
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        sharedViewModel.setCurrentWordId(relatedWord)
    }


    override fun onSynonymChipClicked(synonym: String) {
        sharedViewModel.setCurrentWordId(synonym)
    }

    override fun onAudioClipError(message: String) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_SHORT)
                .configError(context!!, true)
                .show()
    }

    override fun onMerriamWebsterDismissClicked() {
        //TODO set user setting
        adapter.removeWordSource(MerriamWebsterSource::class)
    }

    override fun onDrag(dragFraction: Float, dragTo: Float, rawOffset: Float, rawOffsetPixels: Float, dragDismissScale: Float) {
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

