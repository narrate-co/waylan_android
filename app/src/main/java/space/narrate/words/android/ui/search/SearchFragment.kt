package space.narrate.words.android.ui.search

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
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import space.narrate.words.android.*
import space.narrate.words.android.ui.common.BaseUserFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.smart_suggestion_item.view.*
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.users.UserWordType
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.prefs.RotationManager
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.DelayedLifecycleAction
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
class SearchFragment : BaseUserFragment(), SearchItemAdapter.SearchItemListener, TextWatcher {

    companion object {
        fun newInstance() = SearchFragment()
        const val TAG = "SearchFragment"
    }

    private lateinit var shelfContainer: FrameLayout
    private lateinit var searchContainer: FrameLayout
    private lateinit var searchEditText: AppCompatEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var actionOneImageView: AppCompatImageView
    private lateinit var actionTwoImageView: AppCompatImageView

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

    private val adapter by lazy { SearchItemAdapter(this) }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shelfContainer = view.findViewById(R.id.shelf_container)
        searchContainer = view.findViewById(R.id.search_container)
        searchEditText = view.findViewById(R.id.search_edit_text)
        recyclerView = view.findViewById(R.id.recycler_view)
        actionOneImageView = view.findViewById(R.id.action_one_image_view)
        actionTwoImageView = view.findViewById(R.id.action_two_image_view)


        setUpSearchBar(view)

        setUpRecyclerView(view)

        setUpSmartShelf(view)

        sharedViewModel.currentUserWord.observe(this, Observer {
            if (sharedViewModel.getBackStack().value?.peekOrNull == Navigator.HomeDestination.DETAILS) {
                setShelfActionsForDetails(it)
            }
        })

        sharedViewModel.shouldOpenAndFocusSearch.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { focusAndOpenSearch() }
        })

        setUpShelfActions(view)

        viewModel.shouldShowDetails.observe(this, Observer { event ->
            event.getUnhandledContent()?.let {
                sharedViewModel.onChangeCurrentWord(it)
                Navigator.showDetails(requireActivity())
            }
        })

    }

    override fun onEnterTransactionEnded() {
        // TODO: Does this get called before onViewCreated? If so, remove.
        println("onEnterTransactionEnded")
    }

    // Set up textRes watchers and on focus changed listeners to help control the
    // hiding/showing of both the search sheet and the IME
    private fun setUpSearchBar(view: View?) {
        if (view == null) return

        searchEditText.addTextChangedListener(this)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.expand()
            } else {
                bottomSheetBehavior.collapse(activity)
            }
        }

        searchEditText.setOnClickListener {
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
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        // hide IME if user is scrolling search results
        // This avoids the need to expand the search sheet to the full height of the display
        // and moving results out of "thumb reach"
        recyclerView.setOnTouchListener { _, _ ->
            requireActivity().hideSoftKeyboard()
            false
        }

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
            recyclerView.scrollToPosition(0)
        })
    }

    private fun setUpSmartShelf(view: View?) {
        if (view == null) return

        // TODO remove and replace with automated UI tests
        // If this is a debug build, set the share button to show the smart shelf, alternating the
        // prompt each with each click
        if (BuildConfig.DEBUG) {
            actionTwoImageView.setOnClickListener(object : View.OnClickListener {
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
            val dest = it.peekOrNull ?: Navigator.HomeDestination.HOME
            // wait for the next layout step to grantee the actions.width is correctly captured
            view.post {
                when (dest) {
                    Navigator.HomeDestination.HOME -> {
                        runShelfActionsAnimation(0)
                    }
                    Navigator.HomeDestination.TRENDING -> {
                        // need to know: if current list has a filter applied
                        val hasAppliedFilter = sharedViewModel.getCurrentListFilter().isNotEmpty()
                        runShelfActionsAnimation(if (hasAppliedFilter) 0 else 1)
                        setShelfActionsForList()
                    }
                    Navigator.HomeDestination.DETAILS -> {
                        runShelfActionsAnimation(2)
                        // setShelfActionsForDetails will be called by the observer of
                        // MainViewModel's getCurrentFirestoreUserword()
                    }
                    else -> runShelfActionsAnimation(0)
                }
            }
        })

        // Show the filter action if the current HomeDestination has an applied filter and
        // it is filterable (only TRENDING is filterable currently)
        sharedViewModel.getCurrentListFilterLive().observe(this, Observer {
            if (sharedViewModel.getBackStack().value?.peekOrNull == Navigator.HomeDestination.TRENDING) {
                // Show the filter action if there is no filter applied
                runShelfActionsAnimation(if (it.isEmpty()) 1 else 0)
            }
        })

        // Hide actions if sheet is expanded
        (activity as MainActivity).searchSheetCallback.addOnSlideAction { _, offset ->
            setSheetSlideOffsetForActions(
                    offset,
                    (activity as MainActivity).contextualSheetCallback.currentSlide
            )
        }

        // Hide filter action if contextual sheet is expanded
        (activity as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val currentDest = sharedViewModel.getBackStack().value?.peekOrNull
                    ?: Navigator.HomeDestination.HOME
            if (currentDest == Navigator.HomeDestination.TRENDING) {
                setSheetSlideOffsetForActions(
                        (activity as MainActivity).searchSheetCallback.currentSlide,
                        offset
                )
            }
        }
    }

    private fun setSheetSlideOffsetForActions(searchOffset: Float, contextualOffset: Float) {
        val zeroActions = numberOfShelfActionsShowing == 0
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val keyline3 = resources.getDimensionPixelSize(R.dimen.keyline_3)
        val hiddenMargin = keyline2
        val showingMargin = (((actionOneImageView.width + keyline3) * numberOfShelfActionsShowing) + (if (zeroActions) keyline2 else 0)) + keyline2
        val params = searchContainer.layoutParams  as ConstraintLayout.LayoutParams

        params.rightMargin = MathUtils.normalize(
            Math.max(searchOffset, contextualOffset),
            0F,
            1F,
            showingMargin.toFloat(),
            hiddenMargin.toFloat()
        ).toInt()
        searchContainer.layoutParams = params
    }

    /**
     * A method which can be called from this Fragment's parent Activity. If the parent
     * Activity or other visible Fragments would like to trigger search sheet expanding and
     * IME opening to initiate a search, use this method
     */
    private fun focusAndOpenSearch() {
        bottomSheetBehavior.expand()
        searchEditText.requestFocus()
        requireActivity().showSoftKeyboard(searchEditText)
    }

    /**
     * Expand the smart shelf by adding a view and using a Transition. After the transition
     * has ended, set a [DelayedAfterTransitionEndAction] to close the expanded shelf after
     * a delay
     *
     * TODO Create a custom view to "vertically marquee" textRes that changes in smartSuggestion
     * TODO and animate the removal of the icon/textRes width changing
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
                smartSuggestion.smartButton.text = getString(prompt.message)
                smartSuggestion.smartButton.setIconResource(
                        prompt.icon
                )
                smartSuggestion.smartButton.setOnClickListener {
                    // TODO create a custom smartLabel view that is able to change bounds
                    smartSuggestion.smartButton.text = getString(prompt.checkedText)

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
                smartSuggestion.smartButton.text = getString(prompt.message)
                smartSuggestion.smartButton.setIconResource(
                        prompt.icon
                )
                smartSuggestion.smartButton.setOnClickListener {
                    smartSuggestion.smartButton.text = getString(prompt.checkedText)

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
        val showMargin = (((actionOneImageView.width + keyline3) * numberOfActions) + (if (zeroActions) keyline2 else 0)) + keyline2
        val currentMargin = (searchContainer.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if ((!zeroActions && currentMargin == showMargin)
                || (zeroActions && currentMargin == keyline2)) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = searchContainer.layoutParams as ConstraintLayout.LayoutParams
                params.rightMargin = MathUtils.normalize(
                    interpolatedTime,
                    0F,
                    1F,
                    currentMargin.toFloat(),
                    showMargin.toFloat()
                ).toInt()
                searchContainer.layoutParams = params
            }
        }
        animation.duration = 200
        animation.interpolator = FastOutSlowInInterpolator()
        searchContainer.startAnimation(animation)
    }


    /**
     * Set shelf actions according to [userWord]
     */
    private fun setShelfActionsForDetails(userWord: UserWord?) {
        if (userWord == null)  return

        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)



        actionOneImageView.setOnClickListener {
            sharedViewModel.setCurrentWordFavorited(!isFavorited)
        }

        // TODO create an AVD
        actionOneImageView.swapImageResource(if (isFavorited) {
            R.drawable.ic_round_favorite_24px
        } else {
            R.drawable.ic_round_favorite_border_24px
        })

        // TODO add share button setup
    }

    private fun setShelfActionsForList() {
        actionOneImageView.setOnClickListener {
            sharedViewModel.onShouldOpenContextualFragment()
        }
        actionOneImageView.swapImageResource(R.drawable.ic_round_filter_list_24px)
    }

    override fun onWordClicked(searchItem: SearchItemModel) {
        bottomSheetBehavior.collapse(activity)
        viewModel.onWordClicked(searchItem)
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.onSearchInputTextChanged(s)
    }

    override fun onBannerClicked() {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerLabelClicked() {
        // do nothing. Search banner should not have a label.
    }

    override fun onBannerTopButtonClicked() {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerBottomButtonClicked() {
        // do nothing. Search banner should not have any buttons
    }

}

