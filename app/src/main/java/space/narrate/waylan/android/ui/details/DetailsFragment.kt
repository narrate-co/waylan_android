package space.narrate.waylan.android.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentDetailsBinding
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.ui.search.SearchFragment
import space.narrate.waylan.android.ui.widget.EducationalOverlayView
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemProviderRegistry
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.core.util.configError
import space.narrate.waylan.core.util.configInformative

/**
 * A Fragment to show all details of a word (as it appears in the dictionary). This Fragment
 * handles showing the aggregation of WordSet data, Merriam-Webster data and Firestore data
 * for a given word.
 */
class DetailsFragment: BaseFragment(), DetailAdapterListener {

    private lateinit var binding: FragmentDetailsBinding

    private val navigator: Navigator by inject()

    // The MainViewModel which is used to for data shared between MainActivity and
    // its child fragments (HomeFragment, ListFragment and DetailsFragment)
    private val sharedViewModel: MainViewModel by sharedViewModel()

    // This Fragment's ViewModel
    private val viewModel: DetailsViewModel by viewModel()

    private val detailItemProviderRegistry: DetailItemProviderRegistry by inject()
    private val adapter: DetailItemAdapter = DetailItemAdapter(detailItemProviderRegistry, this)

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
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Postpone enter transition until we've set everything up.
        postponeEnterTransition()

        binding.run {

            appBar.doOnElasticDrag(
                alphaViews = listOf(recyclerView, appBar)
            )

            appBar.doOnElasticDismiss {
                navigator.toBack(Navigator.BackType.DRAG, this.javaClass.simpleName)
            }

            appBar.setOnNavigationIconClicked {
                // Child fragments of MainActivity should report how the user is navigating away
                // from them. For more info, see [BaseFragment.setUnconsumedNavigationMethod]
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

        }

        // Observe the MainViewModel's currentWord. If this changes, it indicates that a user has
        // searched for a different word than is being displayed and this Fragment should
        // react
        sharedViewModel.currentWord.observe(this) {
            binding.appBar.title = it
            // TODO this doesn't work if a list item is loaded after other items.
            binding.recyclerView.scrollToPosition(0)
            viewModel.onCurrentWordChanged(it)
        }

        // Observe all data sources which will be displayed in the [DetailItemAdapter]
        viewModel.list.observe(this) {
            adapter.submitList(it)
        }

        viewModel.shouldShowDragDismissOverlay.observe(this) { event ->
            event.getUnhandledContent()?.let {
                EducationalOverlayView.pullDownEducator(binding.appBar).show()
            }
        }

        viewModel.audioClipAction.observe(this) { event ->
            event.getUnhandledContent()?.let {
                when (it) {
                    is AudioClipAction.Play -> audioClipHelper.play(it.url)
                    is AudioClipAction.Stop -> audioClipHelper.stop()
                }
            }
        }

        viewModel.shouldShowSnackbar.observe(this) { event ->
            event.getUnhandledContent()?.let {
                showSnackbar(it)
            }
        }

        // Start enter transition now that things are set up.
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        binding.coordinatorLayout.setPadding(
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

    override fun onMwThesaurusChipClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
    }

    private fun showSnackbar(model: SnackbarModel) {
        val snackbar = Snackbar.make(
            binding.coordinatorLayout,
            model.textRes,
            when (model.length) {
                SnackbarModel.LENGTH_INDEFINITE -> Snackbar.LENGTH_INDEFINITE
                SnackbarModel.LENGTH_LONG -> Snackbar.LENGTH_LONG
                else -> Snackbar.LENGTH_SHORT
            }
        )

        if (model.isError) {
            snackbar.configError(requireContext())
        } else {
            snackbar.configInformative(requireContext())
        }

        // TODO : Add actions

        snackbar.show()
    }
}

