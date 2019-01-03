package com.wordsdict.android.ui.search

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
import android.view.inputmethod.EditorInfo
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
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.RotationManager
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.ui.common.HeaderBanner
import com.wordsdict.android.util.*
import com.wordsdict.android.util.widget.DelayedLifecycleAction
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.smart_suggestion_item.view.*
import javax.inject.Inject

/**
 * A bottom sheet fragment that handles user search input, current word action items (share,
 * favorite), the search shelf (contextual suggestions which expand into the space above the
 * input bar) and displaying lists of recently searched words if the search field is empty
 * or search results when not.
 *
 * This Fragment is designed to be ergonomic and easy to use one handed. The most popular
 * user journey is to search and define a word. This journey should be as ergonomic, quick and
 * seamless as possible.
 */
class SearchFragment : BaseUserFragment(), SearchAdapter.WordAdapterHandlers, TextWatcher{

    companion object {
        fun newInstance() = SearchFragment()
        const val TAG = "SearchFragment"
    }

    // MainViewModel owned by MainActivity and used to share data between MainActivity
    // and its child Fragments
    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    // SearchFragment's own ViewModel
    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SearchViewModel::class.java)
    }


    // The BottomSheetBehavior of this view.
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    private val adapter by lazy { SearchAdapter(this) }

    // A variable to hold whether or not the space just above the input bar is expanded to
    // show a contextual suggestion
    @Volatile
    private var smartShelfExpanded = false

    // A task to be run after the smart shelf has been expanded. This task is usually to close
    // the smart shelf after a delay has elapsed
    private var smartShelfAfterTransitionEndAction: DelayedAfterTransitionEndAction? = null

    @Inject
    lateinit var rotationManager: RotationManager

    private var numberOfShelfActionsShowing: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onEnterTransactionEnded() {

        setUpSearchBar(view)

        setUpRecyclerView(view)

        setUpSmartShelf(view)

        setUpShelfActions(view)

        sharedViewModel.getCurrentFirestoreUserWord().observe(this, Observer {
            setShelfActionsForDetails(it.userWord)
        })

    }

    // Set up text watchers and on focus changed listeners to help control the
    // hiding/showing of both the search sheet and the IME
    private fun setUpSearchBar(view: View?) {
        if (view == null) return

        view.searchEditText.addTextChangedListener(this)

        // intercept clicks to IME done to avoid taking any action
        view.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            // TODO do something useful if the user is intentionally clicking done
            actionId == EditorInfo.IME_ACTION_DONE
        }

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

    // Set up the recycler view which holds recently viewed words when the search input field
    // is empty and search results when not.
    private fun setUpRecyclerView(view: View?) {
        if (view == null) return
        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

        // hide IME if user is scrolling search results
        // This avoids the need to expand the search sheet to the full height of the display
        // and moving results out of "thumb reach"
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

    // If the list is empty, the user does not have any recent searches. Meaning we can
    // confidently display an onboarding banner explaining the search sheet and its functionality
    private fun setBanner(isListEmpty: Boolean) {
        if (isListEmpty) {
            adapter.setHeader(
                    HeaderBanner(
                            getString(R.string.search_banner_body),
                            null,
                            null
                    )
            )
        } else {
            // remove the header
            adapter.setHeader(null)
        }
    }


    private fun setUpSmartShelf(view: View?) {
        if (view == null) return

        // TODO remove and replace with automated UI tests
        // If this is a debug build, set the share button to show the smart shelf, alternating the
        // prompt each with each click
        if (BuildConfig.DEBUG) {
            view.action2.setOnClickListener(object : View.OnClickListener {
                var clicks = 0
                override fun onClick(v: View?) {
                    val prompt = if (clicks % 2 == 0) {
                        OrientationPrompt.LockToPortrait(Orientation.PORTRAIT)
                    } else {
                        OrientationPrompt.LockToLandscape(Orientation.LANDSCAPE)
                    }
                    expandSmartShelf(prompt)
                    clicks++
                }
            })
        }


        // getOrientationPrompt broadcasts a value once and then immediately broadcasts a null
        // value to avoid observers re-receiving the last value emitted. Listen for broadcasts
        // and display the smart shelf if not null
        viewModel.orientationPrompt.observe(this, Observer {
            if (it != null) {
                DelayedLifecycleAction(this, 500) {
                    expandSmartShelf(it)
                }
            }
        })

        // observe for orientation/rotation changes in the viewModel
        rotationManager.observe(SearchFragment::class.java.simpleName, this, viewModel)

        // observe for all orientation/rotation patterns in the viewModel
        rotationManager.observeForPattern(
                SearchFragment::class.java.simpleName,
                this,
                RotationManager.PATTERNS_ALL,
                viewModel)
    }

    // Shelf actions are actions which live to the right of the search input field. They
    // animate in, compressing the search input field, when the DetailsFragment is the current
    // Fragment and animate out when the search sheet is expanded or DetailsFragment is not the
    // current Fragment
    private fun setUpShelfActions(view: View?) {
        if (view == null) return

        // Hide actions when DetailsFragment is not the current Fragment, otherwise show
        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            // wait for the next layout step to grantee the actions.width is correctly captured
            view.post {
                when (dest) {
                    Navigator.HomeDestination.HOME -> {
                        runShelfActionsAnimation(0)
                    }
                    Navigator.HomeDestination.LIST -> {
                        // need to know: if current list has a filter applied
                        val hasAppliedFilter = sharedViewModel.appliedListFilter.value?.isNotEmpty() ?: false
                        runShelfActionsAnimation(if (hasAppliedFilter) 0 else 1)
                        setShelfActionsForList()
                    }
                    Navigator.HomeDestination.DETAILS -> {
                        runShelfActionsAnimation(2)
                        // setShelfActionsForDetails will be called by the observer of
                        // MainViewModel's getCurrentFirestoreUserword()
                    }
                }
            }
        })

        sharedViewModel.appliedListFilter.observe(this, Observer {
            if (sharedViewModel.getBackStack().value?.peek() == Navigator.HomeDestination.LIST) {
                // Show the filter action if there is no filter applied
                runShelfActionsAnimation(if (it.isEmpty()) 1 else 0)
            }
        })

        // Hide actions if sheet is expanded
        (activity as MainActivity).searchSheetCallback.addOnSlideAction { _, offset ->
            val currentDest = sharedViewModel.getBackStack().value?.peek()
                    ?: Navigator.HomeDestination.HOME
            setSheetSlideOffsetForActions(offset, (activity as MainActivity).contextualSheetCallback.currentSlide)
        }

        action1.setOnClickListener {
            (activity as MainActivity).openContextualFragment()
        }

        // Hide filter action if contextual sheet is expanded
        (activity as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val currentDest = sharedViewModel.getBackStack().value?.peek()
                    ?: Navigator.HomeDestination.HOME
            if (currentDest == Navigator.HomeDestination.LIST) {
                setSheetSlideOffsetForActions((activity as MainActivity).searchSheetCallback.currentSlide, offset)
            }
        }
    }

    private fun setSheetSlideOffsetForActions(searchOffset: Float, contextualOffset: Float) {
        val zeroActions = numberOfShelfActionsShowing == 0
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val hiddenMargin = keyline2
        val showingMargin = (((action1.width + keyline2) * numberOfShelfActionsShowing) + (if (zeroActions) keyline2 else 0)) + keyline2
        val params = search.layoutParams  as ConstraintLayout.LayoutParams

        params.rightMargin = getScaleBetweenRange(
                Math.max(searchOffset, contextualOffset),
                0F,
                1F,
                showingMargin.toFloat(),
                hiddenMargin.toFloat()
        ).toInt()
        search.layoutParams = params
    }

    /**
     * A method which can be called from this Fragment's parent Activity. If the parent
     * Activity or other visible Fragments would like to trigger search sheet expanding and
     * IME opening to initiate a search, use this method
     */
    fun focusAndOpenSearch() {
        bottomSheetBehavior.expand()
        searchEditText.requestFocus()
        activity?.showSoftKeyboard(searchEditText)
    }

    /**
     * Expand the smart shelf by adding a view and using a Transition. After the transition
     * has ended, set a [DelayedAfterTransitionEndAction] to close the expanded shelf after
     * a delay
     *
     * TODO Create a custom view to "vertically marquee" text that changes in smartSuggestion
     * TODO and animate the removal of the icon/text width changing
     */
    private fun expandSmartShelf(prompt: OrientationPrompt) {
        if (view == null) return

        synchronized(smartShelfExpanded) {

            // if the smart shelf is not added to the sheet, add it with a transition
            var smartSuggestion = shelfContainer.getChildAt(0)
            if (smartSuggestion == null) {
                smartShelfAfterTransitionEndAction?.cancel()
                smartShelfAfterTransitionEndAction = null
                TransitionManager.endTransitions(view as ViewGroup)

                val changeBounds = ChangeBounds()
                changeBounds.interpolator = DecelerateInterpolator()
                changeBounds.duration = 200

                smartSuggestion = layoutInflater.inflate(
                        R.layout.smart_suggestion_item,
                        shelfContainer,
                        false
                )
                smartSuggestion.smartLabel.text = getString(prompt.message)
                smartSuggestion.smartImage.setImageDrawable(
                        ContextCompat.getDrawable(context!!, prompt.icon)
                )
                smartSuggestion.setOnClickListener {
                    // TODO create a custom smartLabel view that is able to change bounds
                    smartSuggestion.smartLabel.text = getString(prompt.checkedText)

                    viewModel.setOrientationPreference(prompt.orientationToRequest)
                    (activity?.application as? App)?.updateOrientation()

                }

                val display = activity!!.windowManager.defaultDisplay
                val point = Point()
                display.getSize(point)
                smartSuggestion.measure(point.x, point.y)
                val measuredDiff = smartSuggestion.measuredHeight + smartSuggestion.marginTop + smartSuggestion.marginBottom

                // close the shelf after the transition has ended and after a delay
                smartShelfAfterTransitionEndAction = DelayedAfterTransitionEndAction(
                        this,
                        changeBounds,
                        3000,
                        ::collapseSmartShelf
                )

                //start transition
                TransitionManager.beginDelayedTransition(view as ViewGroup, changeBounds)
                shelfContainer.addView(smartSuggestion)
                bottomSheetBehavior.peekHeight += measuredDiff
                smartShelfExpanded = true
            } else {
                // if the shelf is added, use it to manipulate it's existing prompt
                smartSuggestion.smartLabel.text = getString(prompt.message)
                smartSuggestion.smartImage.setImageDrawable(
                        ContextCompat.getDrawable(context!!, prompt.icon)
                )
                smartSuggestion.setOnClickListener {
                    smartSuggestion.smartLabel.text = getString(prompt.checkedText)

                    viewModel.setOrientationPreference(prompt.orientationToRequest)
                    (activity?.application as? App)?.updateOrientation()
                }
            }
        }
    }

    /**
     * Collapse the expanded smart shelf using a Transition
     */
    private fun collapseSmartShelf() {
        if (view == null) return

        synchronized(smartShelfExpanded) {

            smartShelfAfterTransitionEndAction?.cancel()
            smartShelfAfterTransitionEndAction = null
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

    /**
     * Animate in or out the shelf actions (the actions which live to the right of the search
     * input field) by animating the right margin of the search input layout.
     */
    private fun runShelfActionsAnimation(numberOfActions: Int) {
        if (!isAdded) return

        numberOfShelfActionsShowing = numberOfActions
        val zeroActions = numberOfActions == 0
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val keyline3 = resources.getDimensionPixelSize(R.dimen.keyline_3)
        val showMargin = (((action1.width + keyline3) * numberOfActions) + (if (zeroActions) keyline2 else 0)) + keyline2
        val currentMargin = (search.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if ((!zeroActions && currentMargin == showMargin) || (zeroActions && currentMargin == keyline2)) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = search.layoutParams as ConstraintLayout.LayoutParams
                params.rightMargin = getScaleBetweenRange(
                        interpolatedTime,
                        0F,
                        1F,
                        currentMargin.toFloat(),
                        showMargin.toFloat()
                ).toInt()
                search.layoutParams = params
            }
        }
        animation.duration = 200
        animation.interpolator = FastOutSlowInInterpolator()
        search.startAnimation(animation)
    }


    /**
     * Set shelf actions according to [userWord]
     */
    private fun setShelfActionsForDetails(userWord: UserWord?) {
        if (userWord == null)  return

        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)

        action1.setOnClickListener {
            sharedViewModel.setCurrentWordFavorited(!isFavorited)
        }

        // TODO create an AVD
        action1.swapImageResource(if (isFavorited) {
            R.drawable.ic_round_favorite_24px
        } else {
            R.drawable.ic_round_favorite_border_24px
        })

        // TODO add share button setup
    }

    private fun setShelfActionsForList() {
        action1.setOnClickListener {
            (activity as MainActivity).openContextualFragment()
        }

        action1.swapImageResource(R.drawable.ic_round_filter_list_24px)
    }

    override fun onWordClicked(word: WordSource) {
        bottomSheetBehavior.collapse(activity)
        val id = when (word) {
            is SimpleWordSource -> word.word.word
            is FirestoreUserSource -> word.userWord.word
            is SuggestSource -> word.item.term
            else -> ""
        }
        sharedViewModel.setCurrentWord(id)
        viewModel.logSearchWordEvent(id, word)
        (activity as MainActivity).showDetails()
    }

    override fun onBannerClicked(banner: HeaderBanner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerTopButtonClicked(banner: HeaderBanner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerBottomButtonClicked(banner: HeaderBanner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchInput = s?.toString() ?: ""
    }
}

