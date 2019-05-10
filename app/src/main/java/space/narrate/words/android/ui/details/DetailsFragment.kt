package space.narrate.words.android.ui.details

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.R
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.EducationalOverlayView
import space.narrate.words.android.util.widget.ElasticAppBarBehavior
import space.narrate.words.android.Navigator
import space.narrate.words.android.ui.common.SnackbarModel

/**
 * A Fragment to show all details of a word (as it appears in the dictionary). This Fragment
 * handles showing the aggregation of WordSet data, Merriam-Webster data and Firestore data
 * for a given word.
 */
class DetailsFragment: BaseUserFragment(),
    DetailItemAdapter.Listener,
    ElasticAppBarBehavior.ElasticViewBehaviorCallback {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var statusBarScrim: View
    private lateinit var recyclerView: RecyclerView

    // The MainViewModel which is used to for data shared between MainActivity and
    // its child fragments (HomeFragment, ListFragment and DetailsFragment)
    private val sharedViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)
            .get(MainViewModel::class.java)
    }

    // This Fragment's ViewModel
    private val viewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)
            .get(DetailsViewModel::class.java)
    }

    private val adapter: DetailItemAdapter = DetailItemAdapter(this)

    private val audioClipHelper by lazy {
        AudioClipHelper(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        appBarLayout = view.findViewById(R.id.app_bar)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        statusBarScrim = view.findViewById(R.id.status_bar_scrim)
        recyclerView = view.findViewById(R.id.recycler_view)

        // Add callback to the AppBarLayout's ElasticAppBarBehavior to listen for
        // drag to dismiss events
        ((appBarLayout.layoutParams as CoordinatorLayout.LayoutParams)
            .behavior as ElasticAppBarBehavior)
            .addCallback(this)

        navigationIcon.setOnClickListener {
            // Child fragments of MainActivity should report how the user is navigating away
            // from them. For more info, see [BaseUserFragment.setUnconsumedNavigationMethod]
            setUnconsumedNavigationMethod(NavigationMethod.NAV_ICON)
            requireActivity().onBackPressed()
        }

        // Set up fake status bar background to be either transparent or opaque depending on this
        // Fragment's AppBarLayout offset
        setUpStatusBarScrim(statusBarScrim, appBarLayout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe the MainViewModel's currentWord. If this changes, it indicates that a user has
        // searched for a different word than is being displayed and this Fragment should
        // react
        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.onCurrentWordChanged(it)
        })

        // Observe all data sources which will be displayed in the [DetailItemAdapter]
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })

        viewModel.shouldShowDragDismissOverlay.observe(this, Observer { event ->
            event.getUnhandledContent()?.let {
                EducationalOverlayView.pullDownEducator(appBarLayout).show()
            }
        })

        viewModel.audioClipAction.observe(this, Observer { event ->
            event.getUnhandledContent()?.let {
                when (it) {
                    is AudioClipAction.Play -> audioClipHelper.play(it.url)
                    is AudioClipAction.Stop -> audioClipHelper.stop()
                }
            }
        })

        viewModel.shouldShowSnackbar.observe(this, Observer { event ->
            event.getUnhandledContent()?.let {
                showSnackbar(it)
            }
        })
    }

    override fun onMwRelatedWordClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
    }

    override fun onMwSuggestionWordClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
    }

    override fun onSynonymChipClicked(synonym: String) {
        sharedViewModel.onChangeCurrentWord(synonym)
    }

    override fun onMwAudioPlayClicked(url: String?) {
        viewModel.onPlayAudioClicked(url)
    }

    override fun onMwAudioStopClicked() {
        viewModel.onStopAudioClicked()
    }

    override fun onMwAudioClipError(messageRes: Int) {
        viewModel.onAudioClipError(messageRes)
    }

    override fun onMwPermissionPaneDetailsClicked() {
        Navigator.launchSettings(requireContext())
    }

    override fun onMwPermissionPaneDismissClicked() {
        viewModel.onMerriamWebsterPermissionPaneDismissClicked()
    }

    private fun showSnackbar(model: SnackbarModel) {
        val snackbar = Snackbar.make(coordinatorLayout, model.textRes, when (model.length) {
            SnackbarModel.LENGTH_INDEFINITE -> Snackbar.LENGTH_INDEFINITE
            SnackbarModel.LENGTH_LONG -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        })

        if (model.isError) {
            snackbar.configError(requireContext(), model.abovePeekedSheet)
        } else {
            snackbar.configInformative(requireContext(), model.abovePeekedSheet)
        }

        // TODO : Add actions

        snackbar.show()
    }

    override fun onDrag(
        dragFraction: Float,
        dragTo: Float,
        rawOffset: Float,
        rawOffsetPixels: Float,
        dragDismissScale: Float
    ) {

        // Translate individual views to create a parallax effect
        val alpha = 1 - dragFraction
        val cutDragTo = dragTo * .15F

        appBarLayout.translationY = cutDragTo
        recyclerView.alpha = alpha
        appBarLayout.alpha = alpha
    }

    override fun onDragDismissed(): Boolean {
        setUnconsumedNavigationMethod(NavigationMethod.DRAG_DISMISS)
        Handler().post { requireActivity().onBackPressed() }
        return true
    }

    companion object {
        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "details_fragment_tag"

        fun newInstance() = DetailsFragment()
    }
}

