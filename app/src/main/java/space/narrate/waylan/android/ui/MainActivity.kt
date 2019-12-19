package space.narrate.waylan.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.ui.home.HomeFragment
import space.narrate.waylan.android.ui.list.ListFragment
import space.narrate.waylan.android.ui.search.BottomSheetCallbackCollection
import space.narrate.waylan.android.ui.search.ContextualFragment
import space.narrate.waylan.android.ui.search.SearchFragment
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.hideSoftKeyboard
import space.narrate.waylan.core.util.visible
import kotlin.math.max

/**
 * The main host Activity which displays the perisistent [SearchFragment] bottom sheet as well as a
 * main destination ([HomeFragment], [ListFragment] and [DetailsFragment]).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var searchFragment: SearchFragment
    private lateinit var contextualFragment: ContextualFragment
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var bottomSheetScrimView: View

    private val authenticationStore: AuthenticationStore by inject()

    private val navigator: Navigator by inject()

    /**
     * A single callback aggregator which attached added to [SearchFragment]'s BottomSheetBehavior.
     * Clients wishing to add actions to be run when the bottom sheet is slid or its state has
     * changed can use this property instead of themselves finding the correct
     * [BottomSheetBehavior] and adding a new [BottomSheetBehavior.BottomSheetCallback]
     */
    val searchSheetCallback = BottomSheetCallbackCollection()

    // SearchFragment's BottomSheetBehavior
    private val searchSheetBehavior by lazy {
        BottomSheetBehavior.from(searchFragment.requireView())
    }

    // ContextualFragments's BottomSheetBehavior
    private val contextualSheetBehavior by lazy {
        BottomSheetBehavior.from(contextualFragment.requireView())
    }

    val contextualSheetCallback = BottomSheetCallbackCollection()

    // MainActivity's ViewModel which is also used by its child Fragments to share data
    private val sharedViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        ensureAppHasUser()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tell the system that we'd like to be laid out behind the system bars and handle insets
        // ourselves. This is used because MainActivity's child Fragments use
        // [ElasticAppBarBehavior] and we'd like each fragment to extend to the top
        // of the window. When dragging down the fragment pulls down off the top of the screen,
        // and from under the status bar.
        val decor = window.decorView
        val flags = decor.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        decor.systemUiVisibility = flags

        findNavController().addOnDestinationChangedListener { _, destination, arguments ->
            navigator.setCurrentDestination(destination, arguments)
        }

        searchFragment =
            supportFragmentManager.findFragmentById(R.id.search_fragment) as SearchFragment
        contextualFragment =
            supportFragmentManager.findFragmentById(R.id.contextual_fragment) as ContextualFragment
        coordinatorLayout = findViewById(R.id.coordinator_layout)
        bottomSheetScrimView = findViewById(R.id.bottom_sheet_scrim)

        navigator.shouldNavigateBack.observe(this) { event ->
            event.withUnhandledContent { onBackPressed() }
        }

        sharedViewModel.shouldShowDetails.observe(this) { event ->
            event.withUnhandledContent {
                findNavController().navigate(R.id.action_global_detailsFragment)
            }
        }

        sharedViewModel.nightMode.observe(this) {
            delegate.localNightMode = it.value
        }

        sharedViewModel.orientation.observe(this) {
            setOrientation(it)
        }

        processText(intent)

        setUpScrimView()
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

    private fun ensureAppHasUser() {
        if (!authenticationStore.hasUser) {
            navigator.toAuth(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    fun findNavController(): NavController = findNavController(R.id.nav_host_fragment)

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
        if (searchFragment.handleOnBackPressed()) {
            consumed = true
        }

        if (contextualFragment.handleOnBackPressed()) {
            consumed = true
        }

        return consumed
    }

    private fun setUpScrimView() {

        // Show a scrim behind the search sheet when it is expanded by setting the scrims
        // alpha to match the bottom sheet's slide offset.
        searchSheetCallback.addOnSlideAction { _, searchSlide ->
            setBottomSheetScrimAlpha(searchSlide, contextualSheetCallback.currentSlide)
        }

        // Show a scrim behind the contextual sheet when it is expanded. The scrim should show
        // when either bottom sheet is not resting, hence the use of Math.max
        contextualSheetCallback.addOnSlideAction { _, contextualSlide ->
            setBottomSheetScrimAlpha(searchSheetCallback.currentSlide, contextualSlide)
        }

        // Set the scrims visibility to gone if the search sheet is collapsed, otherwise make it
        // visible
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            setBottomSheetScrimVisibility(newState, contextualSheetCallback.currentState)
        }

        contextualSheetCallback.addOnStateChangedAction { _, newState ->
            setBottomSheetScrimVisibility(searchSheetCallback.currentState, newState)
        }

        // Hide keyboard if sheet is manually dragged and collapsed
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            if (newState == BottomSheetBehavior.STATE_COLLAPSED ||
                    newState == BottomSheetBehavior.STATE_HIDDEN) {
                //make sure keyboard is down
                hideSoftKeyboard()
            }
        }

        bottomSheetScrimView.setOnClickListener {
            searchFragment.close()
            contextualFragment.close()
        }


        contextualSheetBehavior.addBottomSheetCallback(contextualSheetCallback)
        searchSheetBehavior.addBottomSheetCallback(searchSheetCallback)
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
        bottomSheetScrimView.alpha = max(searchSheetSlide, contextualSheetSlide)
    }

    private fun setOrientation(orientation: Orientation) {
        requestedOrientation = orientation.value
    }
}
