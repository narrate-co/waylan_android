package space.narrate.words.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import space.narrate.words.android.ui.common.BaseUserActivity
import space.narrate.words.android.ui.list.ListFragment
import space.narrate.words.android.ui.search.ContextualFragment
import space.narrate.words.android.ui.search.SearchFragment
import space.narrate.words.android.ui.search.BottomSheetCallbackCollection
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.KeyboardManager
import space.narrate.words.android.ui.list.ListType

/**
 * The main host Activity which displays the perisistent [SearchFragment] bottom sheet as well as a
 * main destination ([HomeFragment], [ListFragment] and [DetailsFragment]).
 */
class MainActivity : BaseUserActivity() {

    private lateinit var searchFragment: SearchFragment
    private lateinit var contextualFragment: ContextualFragment
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var bottomSheetScrimView: View


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

        searchFragment =
            supportFragmentManager.findFragmentById(R.id.search_fragment) as SearchFragment
        contextualFragment =
            supportFragmentManager.findFragmentById(R.id.contextual_fragment) as ContextualFragment
        coordinatorLayout = findViewById(R.id.coordinator_layout)
        bottomSheetScrimView = findViewById(R.id.bottom_sheet_scrim)

        sharedViewModel.shouldShowDetails.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { showDetails() }
        })

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
     * in [onCreate] or [onNewIntent]. A process textRes extra represents an intent fired after the
     * user has selected textRes outside of Words and used the tooltip menu item 'Words' to indicate
     * that they would like the textRes defined.
     */
    private fun processText(intent: Intent?) {
        sharedViewModel.onProcessText(
            intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        )
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
        sharedViewModel.onNavigatedFrom(unconsumedNavigationMethod)
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
        val maxReachableExpandedHeight = Math.round(displayHeightPx * .60F)
        val searchItemHeight = getDimensionPixelSizeFromAttr(android.R.attr.listPreferredItemHeight)
        val minPeekHeight = resources.getDimensionPixelSize(R.dimen.search_min_peek_height)
        val minVisibleHeightAboveKeyboard = minPeekHeight + (1.5 * searchItemHeight)

        searchFragment.view?.layoutParams?.height = maxReachableExpandedHeight

        // Observe the height of the keyboard. If it is taller than the search bar + 1.5 search
        // result list items (keep a few list items visible so the user knows there are immediate
        // results), reset the height of the search sheet.
        KeyboardManager(this, coordinatorLayout)
                .getKeyboardHeightData()
                .observe(this, Observer {
                    val minHeight = Math.max(
                            maxReachableExpandedHeight,
                            (it.height + minVisibleHeightAboveKeyboard).toInt()
                    )
                    if (it.height != 0 && minHeight != searchFragment.view?.layoutParams?.height) {
                        searchFragment.view?.layoutParams?.height = minHeight
                    }
                })


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
        bottomSheetScrimView.setOnClickListener {
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
            bottomSheetScrimView.gone()
        } else {
            bottomSheetScrimView.visible()
        }
    }

    private fun setBottomSheetScrimAlpha(searchSheetSlide: Float, contextualSheetSlide: Float) {
        bottomSheetScrimView.alpha = Math.max(searchSheetSlide, contextualSheetSlide)
    }

    private fun showHome() {
        if (Navigator.showHome(this)) {
            sharedViewModel.onNavigatedTo(Navigator.HomeDestination.HOME)
        }
    }

    fun showDetails() {
        if (Navigator.showDetails(this)) {
            sharedViewModel.onNavigatedTo(Navigator.HomeDestination.DETAILS)
        }
    }

    fun showListFragment(type: ListType) {
        if (Navigator.showListFragment(this, type)) {
            sharedViewModel.onNavigatedTo(type.homeDestination)
        }
    }

    fun launchSettings() = Navigator.launchSettings(this)

}
