package space.narrate.words.android.ui.details

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.R
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.data.repository.MerriamWebsterSource
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*
import space.narrate.words.android.util.widget.EducationalOverlayView
import space.narrate.words.android.util.widget.ElasticAppBarBehavior
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import space.narrate.words.android.Navigator

/**
 * A Fragment to show all details of a word (as it appears in the dictionary). This Fragment
 * handles showing the aggregation of WordSet data, Merriam-Webster data and Firestore data
 * for a given word.
 */
class DetailsFragment: BaseUserFragment(),
        DetailsAdapter.Listener,
        ElasticAppBarBehavior.ElasticViewBehaviorCallback {

    companion object {
        private val TAG = DetailsFragment::class.java.simpleName

        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "details_fragment_tag"

        fun newInstance() = DetailsFragment()
    }

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

    private val adapter: DetailsAdapter = DetailsAdapter(this)

    private val audioClipHelper by lazy {
        AudioClipHelper(requireContext(), this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        // Add callback to the AppBarLayout's ElasticAppBarBehavior to listen for
        // drag to dismiss events
        ((view.appBar
                .layoutParams as CoordinatorLayout.LayoutParams)
                .behavior as ElasticAppBarBehavior)
                .addCallback(this)

        view.navigationIcon.setOnClickListener {
            // Child fragments of MainActivity should report how the user is navigating away
            // from them. For more info, see [BaseUserFragment.setUnconsumedNavigationMethod]
            setUnconsumedNavigationMethod(NavigationMethod.NAV_ICON)
            activity?.onBackPressed()
        }

        // Set up fake status bar background to be either transparent or opaque depending on this
        // Fragment's AppBarLayout offset
        setUpStatusBarScrim(view.statusBarScrim, view.appBar)

        return view
    }

    // defer load intensive work until after enter transition has ended
    override fun onEnterTransitionEnded() {
        setUpRecyclerView()

        // Observe the MainViewModel's currentWord. If this changes, it indicates that a user has
        // searched for a different word than is being displayed and this Fragment should
        // react
        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.setWord(it)
            sharedViewModel.setCurrentWordRecented()
        })

        // Observe all data sources which will be displayed in the [DetailsAdapter]

        viewModel.wordPropertiesSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
        viewModel.wordsetSource.observe(this, Observer {
            if (it != null) {
                adapter.submitWordSource(it)
            }
        })
        viewModel.firestoreUserSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
        viewModel.firestoreGlobalSource.observe(this, Observer {
            adapter.submitWordSource(it)
        })
        viewModel.merriamWebsterSource.observe(this, Observer {
            if (it.wordsDefinitions.user?.merriamWebsterState?.isValid == true
                    || !viewModel.hasSeenMerriamWebsterPermissionsPane) {
                adapter.submitWordSource(it)
            }
        })

        if (!viewModel.hasSeenDragDismissOverlay) {
            EducationalOverlayView.pullDownEducator(appBar).show()
            viewModel.hasSeenDragDismissOverlay = true
        }

    }

    private fun setUpRecyclerView() {
        recyclerView?.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView?.adapter = adapter
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        sharedViewModel.setCurrentWord(relatedWord)
    }

    override fun onSuggestionWordClicked(suggestionWord: String) {
        sharedViewModel.setCurrentWord(suggestionWord)
    }

    override fun onSynonymChipClicked(synonym: String) {
        sharedViewModel.setCurrentWord(synonym)
    }

    override fun onPlayAudioClicked(url: String?) {
        audioClipHelper.play(url)
    }

    override fun onStopAudioClicked() {
        audioClipHelper.stop()
    }

    override fun onAudioClipError(message: String) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_SHORT)
                .configError(context!!, true)
                .show()
    }

    override fun onMerriamWebsterDetailsClicked() {
        Navigator.launchSettings(requireContext())
    }

    override fun onMerriamWebsterDismissClicked() {
        viewModel.hasSeenMerriamWebsterPermissionsPane = true
        adapter.removeWordSource(MerriamWebsterSource::class)
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

        view?.appBar?.translationY = cutDragTo
        view?.recyclerView?.alpha = alpha
        view?.appBar?.alpha = alpha
    }

    override fun onDragDismissed(): Boolean {
        setUnconsumedNavigationMethod(NavigationMethod.DRAG_DISMISS)
        Handler().post { activity?.onBackPressed() }
        return true
    }

}

