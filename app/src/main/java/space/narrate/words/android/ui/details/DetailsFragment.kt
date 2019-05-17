package space.narrate.words.android.ui.details

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.EducationalOverlayView
import space.narrate.words.android.util.widget.ElasticAppBarBehavior
import space.narrate.words.android.ui.common.SnackbarModel
import space.narrate.words.android.ui.search.SearchFragment
import space.narrate.words.android.util.widget.ElasticTransition

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = ElasticTransition()
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

        // Postpone enter transition until we've set everything up.
        postponeEnterTransition()

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

        // Start enter transition now that things are set up.
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.setPadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight,
            SearchFragment.getPeekHeight(requireContext(), insets)
        )
        return super.handleApplyWindowInsets(insets)
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
        findNavController().navigate(R.id.action_detailsFragment_to_settingsFragment)
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
            snackbar.configError(requireContext())
        } else {
            snackbar.configInformative(requireContext())
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
        sharedViewModel.onDragDismissBackEvent(DetailsFragment::class.java.simpleName)
        Handler().post { requireActivity().onBackPressed() }
        return true
    }
}

