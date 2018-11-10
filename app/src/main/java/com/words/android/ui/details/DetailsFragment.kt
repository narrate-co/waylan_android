package com.words.android.ui.details

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.users.UserWordType
import com.words.android.data.repository.WordSource
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.ElasticViewBehavior
import com.words.android.util.configError
import kotlinx.android.synthetic.main.details_fragment.*
import kotlinx.android.synthetic.main.details_fragment.view.*

class DetailsFragment: BaseUserFragment(), Toolbar.OnMenuItemClickListener, DetailsAdapter.Listener, ElasticViewBehavior.ElasticViewBehaviorCallback {


    companion object {
        const val FRAGMENT_TAG = "details_fragment_tag"
        fun newInstance() = DetailsFragment()
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }


    private val adapter: DetailsAdapter = DetailsAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.details_fragment, container, false)
//        view.toolbar.inflateMenu(R.menu.details_menu)
//        view.toolbar.setOnMenuItemClickListener(this)
        val elastic = (view.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior as ElasticViewBehavior
        elastic.addCallback(this)
        view.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        return view
    }

    // defer load intensive work until after the fragment transaction has ended
    override fun onEnterTransactionEnded() {
        setUpRecyclerView()

        sharedViewModel.currentSources.observe(this, Observer { source ->
            sharedViewModel.setCurrentWordRecented()
//            when (source) {
//                is WordSource.FirestoreUserSource -> {
//                    setUserWord(source.userWord)
//                }
//            }
            adapter.submitWordSource(source)
        })
    }


    private fun setUpRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView?.adapter = adapter
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_favorite -> {
                sharedViewModel.setCurrentWordFavorited(!item.isChecked)
                true
            }
            //TODO implement share?
            else -> false
        }
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        sharedViewModel.setCurrentWordId(relatedWord)
    }


    override fun onSynonymChipClicked(synonym: String) {
        sharedViewModel.setCurrentWordId(synonym)
    }

    override fun onAudioClipError(message: String) {
        Snackbar.make(detailsRoot, message, Snackbar.LENGTH_SHORT)
                .configError(context!!, true)
                .show()
    }

    override fun onMerriamWebsterDismissClicked() {
        //TODO set user setting
        adapter.removeWordSource(WordSource.MerriamWebsterSource::class)
    }

    override fun onDrag(dragFraction: Float, dragTo: Float, rawOffset: Float, rawOffsetPixels: Float, dragDismissScale: Float) {
        //TODO change alpha and translationY of other views
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

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null)  return

        val favoriteMenuItem = toolbar.menu?.findItem(R.id.action_favorite)
        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)
        favoriteMenuItem?.isChecked = isFavorited
        favoriteMenuItem?.icon = ContextCompat.getDrawable(context!!, if (isFavorited) R.drawable.ic_round_favorite_24px else R.drawable.ic_round_favorite_border_24px)

    }



}

