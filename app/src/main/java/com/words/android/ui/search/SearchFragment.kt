package com.words.android.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.*
import com.words.android.ui.common.BaseUserFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.users.UserWordType
import com.words.android.data.repository.FirestoreUserSource
import com.words.android.data.repository.SimpleWordSource
import com.words.android.data.repository.SuggestSource
import com.words.android.data.repository.WordSource
import com.words.android.util.collapse
import com.words.android.util.expand
import com.words.android.util.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : BaseUserFragment(), SearchAdapter.WordAdapterHandlers, TextWatcher{

    companion object {
        fun newInstance() = SearchFragment()
        const val TAG = "SearchFragment"
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SearchViewModel::class.java)
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    private val adapter by lazy { SearchAdapter(this) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)


        setUpSearchBar(view)

        setUpRecyclerView(view)

        setUpShelfActions(view)

        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.setWordId(it)
        })

        viewModel.firestoreUserSource.observe(this, Observer { source ->
            setUserWord(source.userWord)
        })

        return view
    }

    override fun onWordClicked(word: WordSource) {
        bottomSheetBehavior.collapse(activity)
        val id = when (word) {
            is SimpleWordSource -> word.word.word
            is FirestoreUserSource -> word.userWord.word
            is SuggestSource -> word.item.term
            else -> ""
        }
        sharedViewModel.setCurrentWordId(id)
        viewModel.logSearchWordEvent(id, word)
        (activity as MainActivity).showDetails()
    }

    private fun setUpSearchBar(view: View) {

        view.searchEditText.addTextChangedListener(this)

        view.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.expand()
            } else {
                bottomSheetBehavior.collapse(activity)
            }
        }

        view.searchEditText.setOnClickListener {
            if (searchEditText.hasFocus()) {
                bottomSheetBehavior.expand()
            }
        }
    }

    private fun setUpRecyclerView(view: View) {
        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

        // hide keyboard if scrolling search results
        view.recycler.setOnTouchListener { v, event ->
            (activity as MainActivity).hideSoftKeyboard()
            false
        }

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
            view.recycler.scrollToPosition(0)
        })
    }

    private fun setUpShelfActions(view: View) {

        // Hide actions when not in details
        sharedViewModel.getBackStack().observe(this, Observer {
            println("$TAG::getHomeDestination - $it")
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            // wait for the next layout step to grantee the actions.width is correctly captured
            view.post {
                when (dest) {
                    Navigator.HomeDestination.HOME, Navigator.HomeDestination.LIST -> runActionsAnimation(false)
                    Navigator.HomeDestination.DETAILS -> runActionsAnimation(true)
                }
            }
        })

        // Hide actions if sheet is expanded
        (activity as MainActivity).searchSheetCallback.addOnSlideAction { view, offset ->
            val currentDest = sharedViewModel.getBackStack().value?.peek()
                    ?: Navigator.HomeDestination.HOME
            if (currentDest == Navigator.HomeDestination.DETAILS) {
                val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
                val hideMargin = keyline2
                val showMargin = actions.width + keyline2
                val params = search.layoutParams as ConstraintLayout.LayoutParams
                val adjustedInterpolatedTime = 1.0F - offset
                params.rightMargin = Math.max(hideMargin, (showMargin * adjustedInterpolatedTime).toInt())
                search.layoutParams = params
            }
        }
    }

    private fun runActionsAnimation(show: Boolean) {
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val hideMargin = keyline2
        val showMargin = actions.width + keyline2
        val currentMargin = (search.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if ((show && currentMargin == showMargin) || (!show && currentMargin == hideMargin)) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = search.layoutParams as ConstraintLayout.LayoutParams
                val adjustedInterpolatedTime: Float = if (show) interpolatedTime else (1.0F - interpolatedTime)
                params.rightMargin = Math.max(hideMargin, (showMargin * adjustedInterpolatedTime).toInt())
                search.layoutParams = params
            }
        }
        animation.duration = 200
        animation.interpolator = FastOutSlowInInterpolator()
        search.startAnimation(animation)
    }

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null)  return

        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)

        favorite.setOnClickListener {
            sharedViewModel.setCurrentWordFavorited(!isFavorited)
        }

        favorite?.setImageResource(if (isFavorited) R.drawable.ic_round_favorite_24px else R.drawable.ic_round_favorite_border_24px)
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchInput = s?.toString() ?: ""
    }
}

