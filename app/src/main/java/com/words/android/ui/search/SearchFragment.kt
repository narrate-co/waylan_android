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
import com.words.android.util.collapse
import com.words.android.util.expand
import com.words.android.util.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : BaseUserFragment(), WordsAdapter.WordAdapterHandlers, TextWatcher{

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

    private val adapter by lazy { WordsAdapter(this) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

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

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
        })

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

        (activity as MainActivity).searchSheetCallback.addOnStateChangedAction { view, newState ->
            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                //make sure keyboard is down
                (activity as MainActivity).hideSoftKeyboard()
            }
        }

        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.setWordId(it)
        })

        viewModel.firestoreUserSource.observe(this, Observer { source ->
            setUserWord(source.userWord)
        })

        return view
    }

    override fun onWordClicked(word: String) {
        bottomSheetBehavior.collapse(activity)
        sharedViewModel.setCurrentWordId(word)
        (activity as MainActivity).showDetails()
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

