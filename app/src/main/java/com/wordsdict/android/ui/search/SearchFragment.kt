package com.wordsdict.android.ui.search

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wordsdict.android.*
import com.wordsdict.android.ui.common.BaseUserFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.wordsdict.android.data.firestore.users.UserWord
import com.wordsdict.android.data.firestore.users.UserWordType
import com.wordsdict.android.data.prefs.RotationManager
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.util.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.smart_suggestion_item.view.*
import javax.inject.Inject

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

    @Volatile
    var smartShelfExpanded = false

    var smartShelfTransitionEndAction: TransitionEndAction? = null

    @Inject
    lateinit var rotationManager: RotationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        return view
    }

    override fun onEnterTransactionEnded() {

        setUpSearchBar(view)

        setUpRecyclerView(view)

        setUpSmartShelf(view)

        setUpShelfActions(view)

        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.setWordId(it)
        })

        viewModel.firestoreUserSource.observe(this, Observer { source ->
            setUserWord(source.userWord)
        })
    }

    private fun setUpSearchBar(view: View?) {
        if (view == null) return

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

    private fun setUpRecyclerView(view: View?) {
        if (view == null) return
        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

        // hide keyboard if scrolling search results
        view.recycler.setOnTouchListener { _, _ ->
            activity?.hideSoftKeyboard()
            false
        }

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
            setBanner(it.isEmpty())
            view.recycler.scrollToPosition(0)
        })
    }

    private fun setBanner(isListEmpty: Boolean) {
        if (isListEmpty) {
            adapter.setHeader(Banner(getString(R.string.search_banner_body), null, null))
        } else {
            adapter.setHeader(null)
        }
    }

    private fun setUpSmartShelf(view: View?) {
        if (view == null) return

        viewModel.getOrientationPrompt().observe(this, Observer {
            if (it != null) {
                runDelayed(500) {
                    expandSmartShelf(it)
                }
            }
        })

        rotationManager.observe(SearchFragment::class.java.simpleName, this, viewModel)

        rotationManager.observeForPattern(SearchFragment::class.java.simpleName, this, RotationManager.PATTERNS_ALL, viewModel)
    }

    private fun setUpShelfActions(view: View?) {
        if (view == null) return

        // Hide actions when not in details
        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            // wait for the next layout step to grantee the actions.width is correctly captured
            view.post {
                when (dest) {
                    Navigator.HomeDestination.HOME, Navigator.HomeDestination.LIST -> runShelfActionsAnimation(false)
                    Navigator.HomeDestination.DETAILS -> runShelfActionsAnimation(true)
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

    fun focusAndOpenSearch() {
        bottomSheetBehavior.expand()
        searchEditText.requestFocus()
        activity?.showSoftKeyboard(searchEditText)
    }

    //TODO make this more robust
    //TODO calling this while expanded should just change the smartSuggestion text with an animation,
    //TODO skipping the peekHeight animation
    private fun expandSmartShelf(prompt: OrientationPrompt) {
        if (view == null) return

        synchronized(smartShelfExpanded) {
            smartShelfTransitionEndAction?.cancel()
            smartShelfTransitionEndAction = null
            TransitionManager.endTransitions(view as ViewGroup)

            val changeBounds = ChangeBounds()
            changeBounds.interpolator = DecelerateInterpolator()
            changeBounds.duration = 200

            val smartSuggestion = layoutInflater.inflate(R.layout.smart_suggestion_item, shelfContainer, false)
            smartSuggestion.smartLabel.text = getString(prompt.message)
            smartSuggestion.smartImage.setImageDrawable(ContextCompat.getDrawable(context!!, prompt.icon))
            smartSuggestion.setOnClickListener {
                //TODO create a custom smartLabel view that is able to change bounds
                smartSuggestion.smartLabel.text = "Locked"

                viewModel.orientation = prompt.orientationToRequest
                (activity?.application as? App)?.updateOrientation()

            }
            val display = activity!!.windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            smartSuggestion.measure(point.x, point.y)
            val measuredDiff = smartSuggestion.measuredHeight + smartSuggestion.marginTop + smartSuggestion.marginBottom
            smartShelfTransitionEndAction = TransitionEndAction(this, changeBounds, 3000, ::collapseSmartShelf)

            //start transition
            TransitionManager.beginDelayedTransition(view as ViewGroup, changeBounds)
            shelfContainer.addView(smartSuggestion)
            bottomSheetBehavior.peekHeight += measuredDiff
            smartShelfExpanded = !smartShelfExpanded
        }
    }

    private fun collapseSmartShelf() {
        if (view == null) return

        synchronized(smartShelfExpanded) {

            smartShelfTransitionEndAction?.cancel()
            smartShelfTransitionEndAction = null
            TransitionManager.endTransitions(view as ViewGroup)
            val measuredDiff = shelfContainer.height

            val changeBounds = ChangeBounds()
            changeBounds.interpolator = DecelerateInterpolator()
            changeBounds.duration = 200

            //start transition
            TransitionManager.beginDelayedTransition(view as ViewGroup, changeBounds)
            shelfContainer.removeAllViews()
            bottomSheetBehavior.peekHeight -= measuredDiff
            smartShelfExpanded = !smartShelfExpanded
        }

    }

    private fun runShelfActionsAnimation(show: Boolean) {
        if (!isAdded) return

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

    override fun onBannerClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerTopButtonClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerBottomButtonClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchInput = s?.toString() ?: ""
    }
}

