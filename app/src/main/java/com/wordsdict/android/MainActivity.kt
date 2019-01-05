package com.wordsdict.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wordsdict.android.ui.common.BaseUserActivity
import com.wordsdict.android.ui.list.ListFragment
import com.wordsdict.android.ui.search.ContextualFragment
import com.wordsdict.android.ui.search.SearchFragment
import com.wordsdict.android.ui.search.BottomSheetCallbackCollection
import com.wordsdict.android.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * The main host Activity which displays the perisistent [SearchFragment] bottom sheet as well as a
 * main destination ([HomeFragment], [ListFragment] and [DetailsFragment]).
 */
class MainActivity : BaseUserActivity() {

    /**
     * A single callback aggregator which attached added to [SearchFragment]'s BottomSheetBehavior.
     * Clients wishing to add actions to be run when the bottom sheet is slid or its state has
     * changed can use this property instead of themselves finding the correct
     * [BottomSheetBehavior] and adding a new [BottomSheetBehavior.BottomSheetCallback]
     */
    val searchSheetCallback = BottomSheetCallbackCollection()

    // SearchFragment's BottomSheetBehavior
    private val searchSheetBehavior by lazy {
        BottomSheetBehavior.from(searchFragment.view)
    }

    // ContextualFragments's BottomSheetBehavior
    private val contextualSheetBehavior by lazy {
        BottomSheetBehavior.from(contextualFragment.view)
    }

    val contextualSheetCallback = BottomSheetCallbackCollection()

    // MainActivity's ViewModel which is also used by its child Fragments to share data
    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Tell the system that we'd like to be laid out behind the system bars and handle insets
        // ourselves. This is used because MainActivity's child Fragments use
        // [ElasticAppBarBehavior] and we'd like each fragment to extend to the top
        // of the window. When dragging down the fragment pulls down off the top of the screen,
        // and from under the status bar.
        window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            showHome()
        }

        processText(intent)

        setUpSearchSheet()

        setUpContextualSheet()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processText(intent)
    }

    /**
     * Handle an intent that contains [Intent.ACTION_PROCESS_TEXT] given to this Activity either
     * in [onCreate] or [onNewIntent]. A process text extra represents an intent fired after the
     * user has selected text outside of Words and used the tooltip menu item 'Words' to indicate
     * that they would like the text defined.
     */
    private fun processText(intent: Intent?) {
        val textToProcess = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        if (!textToProcess.isNullOrBlank()) {
            sharedViewModel.setCurrentWord(textToProcess.toString())
            showDetails()
        }
    }

    /**
     * This method expects to receive all back events from all child Fragments and back
     * navigation methods. It determines a) if the back event should close an opened
     * [SearchFragment] or IME, b) maintaining an internal representation of this hosts Fragment
     * back stack, c) reporting [NavigationMethod]s and d) calling [onBackPressed] propperly.
     *
     * All child Fragments should call through to this method to handle back events instead of
     * calling the Activity's [getSupportFragmentManager.popBackStack].
     */
    override fun onBackPressed() {
        if (maybeConsumeOnBackPressed()) return
        sharedViewModel.popBackStack(unconsumedNavigationMethod)
        unconsumedNavigationMethod = null
        super.onBackPressed()
    }

    /**
     * Perform actions on Views/Fragments which are not added to the back stack but should maybe
     * consume back events in certain states.
     *
     * @return true if any View or Fragment has consumed this back event and no further modification
     *  to the back stack should take place.
     */
    private fun maybeConsumeOnBackPressed(): Boolean {
        var consumed = false
        if (searchSheetBehavior.collapse(this)) {
            consumed = true
        }
        if (contextualSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED &&
                contextualSheetBehavior.collapse(this)) {
            consumed = true
        }

        return consumed
    }

    /**
     * Configure [SearchFragment] and its [BottomSheetBehavior]
     */
    private fun setUpSearchSheet() {

        // Set max expanded height to 60% of screen height, the max height it can be expected that
        // a person can reach with their thumb
        searchFragment.view?.layoutParams?.height = Math.round(displayHeightPx * .60F)

        // Show a scrim behind the search sheet when it is expanded by setting the scrims
        // alpha to match the bottom sheet's slide offset.
        searchSheetCallback.addOnSlideAction { _, searchSlide ->
            setBottomSheetScrimAlpha(searchSlide, contextualSheetCallback.currentSlide)
        }

        // Set the scrims visibility to gone if the search sheet is collapsed, otherwise make it
        // visible
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            setBottomSheetScrimVisibility(newState, contextualSheetCallback.currentState)
        }

        // Hide keyboard if sheet is manually dragged and collapsed
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            if (newState == BottomSheetBehavior.STATE_COLLAPSED ||
                    newState == BottomSheetBehavior.STATE_HIDDEN) {
                //make sure keyboard is down
                hideSoftKeyboard()
            }
        }

        // collapse the search sheet if it's scrim is touched
        bottomSheetSkrim.setOnClickListener {
            searchSheetBehavior.collapse(this)
            contextualSheetBehavior.collapse(this)
        }

        // add the search sheet callback to the bottom sheet (note we are still able to add
        // [onStateChangedAction] and [onSlideActions] after adding if needs, as are other
        // clients)
        searchSheetBehavior.setBottomSheetCallback(searchSheetCallback)
    }

    /**
     * Configure [ContextualFragment] and its [BottomSheetBehavior]
     */
    private fun setUpContextualSheet() {

        contextualSheetBehavior.isFitToContents = true

        // Set max expanded height to 60% of screen height plus a 52dp offset to be visible
        // above the search sheet when both sheets are peeking

        // Show a scrim behind the contextual sheet when it is expanded. The scrim should show
        // when either bottom sheet is not resting, hence the use of Math.max
        contextualSheetCallback.addOnSlideAction { _, contextualSlide ->
            setBottomSheetScrimAlpha(searchSheetCallback.currentSlide, contextualSlide)
        }

        contextualSheetCallback.addOnStateChangedAction { _, newState ->
            setBottomSheetScrimVisibility(searchSheetCallback.currentState, newState)
        }


        contextualSheetBehavior.setBottomSheetCallback(contextualSheetCallback)
    }

    /**
     * Set the bottomSheetScrim's visibility depending on the state of both the search and
     * contextual bottom sheet. If both <= [BottomSheetBehavior.STATE_COLLAPSED], the scrim will
     * be set to gone. otherwise, it will be visible.
     */
    private fun setBottomSheetScrimVisibility(searchSheetState: Int, contextualSheetState: Int) {
        if ((searchSheetState == BottomSheetBehavior.STATE_COLLAPSED
                        || searchSheetState == BottomSheetBehavior.STATE_HIDDEN)
                && (contextualSheetState == BottomSheetBehavior.STATE_COLLAPSED
                        || contextualSheetState == BottomSheetBehavior.STATE_HIDDEN)
        ) {
            bottomSheetSkrim.gone()
        } else {
            bottomSheetSkrim.visible()
        }
    }

    private fun setBottomSheetScrimAlpha(searchSheetSlide: Float, contextualSheetSlide: Float) {
        bottomSheetSkrim.alpha = Math.max(searchSheetSlide, contextualSheetSlide)
    }


    /**
     * Any child Fragment of this Activity can call this method to have the SearchFragment
     * bottom sheet expanded, the SearchFragment's search input field focused and the IME opened to
     * initiate a new search.
     *
     * This is used by [ListFragment] when a user clicks on a banner that prompts them to
     * "Get Started" searching for words.
     */
    fun focusAndOpenSearch() {
        val searchFragment = supportFragmentManager
                .findFragmentById(R.id.searchFragment) as? SearchFragment
        searchFragment?.focusAndOpenSearch()
    }

    fun openContextualFragment() {
        val contextualFragment = supportFragmentManager
                .findFragmentById(R.id.contextualFragment) as? ContextualFragment
        contextualFragment?.expand()
    }

    private fun showHome() {
        if (Navigator.showHome(this)) {
            sharedViewModel.pushToBackStack(Navigator.HomeDestination.HOME)
        }
    }

    fun showDetails() {
        if (Navigator.showDetails(this)) {
            sharedViewModel.pushToBackStack(Navigator.HomeDestination.DETAILS)
        }
    }

    fun showListFragment(type: ListFragment.ListType) {
        if (Navigator.showListFragment(this, type)) {
            sharedViewModel.pushToBackStack(type.homeDestination)
        }
    }

    fun launchSettings() = Navigator.launchSettings(this)

}
