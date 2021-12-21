package space.narrate.waylan.android.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentDetailsBinding
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.ui.widget.EducationalOverlayView
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemProviderRegistry
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.TransitionType
import space.narrate.waylan.core.ui.common.AudioClipAction
import space.narrate.waylan.core.ui.common.AudioClipHelper
import space.narrate.waylan.core.ui.common.SnackbarModel
import space.narrate.waylan.core.util.FastOutUltraSlowIn
import space.narrate.waylan.core.util.make
import space.narrate.waylan.core.util.themeColor

/**
 * A Fragment to show all details of a word (as it appears in the dictionary). This Fragment
 * handles showing the aggregation of WordSet data, Merriam-Webster data and Firestore data
 * for a given word.
 */
class DetailsFragment: Fragment(), DetailAdapterListener {

    private lateinit var binding: FragmentDetailsBinding

    private val navigator: Navigator by inject()

    private val args: DetailsFragmentArgs by lazy { navArgs<DetailsFragmentArgs>().value }

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
        setUpTransitions(args.transitionType)
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

        binding.artView.animateSmear(true)
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

        val originalRecyclerViewPadding = binding.recyclerView.paddingBottom
        sharedViewModel.keyboardHeight.observe(viewLifecycleOwner) {
            binding.recyclerView.updatePadding(
                bottom = (originalRecyclerViewPadding + it).toInt()
            )
        }

        // Observe the MainViewModel's currentWord. If this changes, it indicates that a user has
        // searched for a different word than is being displayed and this Fragment should
        // react
        sharedViewModel.currentWord.observe(viewLifecycleOwner) {
            binding.appBar.title = it
            viewModel.onCurrentWordChanged(it)
        }

        // Observe all data sources which will be displayed in the [DetailItemAdapter]
        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.shouldShowDragDismissOverlay.observe(viewLifecycleOwner) { event ->
            event.withUnhandledContent {
                EducationalOverlayView.pullDownEducator(binding.appBar).show()
            }
        }

        viewModel.audioClipAction.observe(viewLifecycleOwner) { event ->
            event.withUnhandledContent {
                when (it) {
                    is AudioClipAction.Play -> audioClipHelper.play(it.url)
                    is AudioClipAction.Stop -> audioClipHelper.stop()
                }
            }
        }

        viewModel.shouldShowSnackbar.observe(viewLifecycleOwner) { event ->
            event.withUnhandledContent {
                showSnackbar(it)
            }
        }

        // Start enter transition now that things are set up.
        startPostponedEnterTransition()
    }

    private fun setUpTransitions(type: TransitionType) {
        when (type) {
            TransitionType.CONTAINER_TRANSFORM -> {
                sharedElementEnterTransition = MaterialContainerTransform().apply {
                    scrimColor = AppCompatResources.getColorStateList(
                        requireContext(),
                        R.color.scrim
                    ).defaultColor
                    interpolator = FastOutUltraSlowIn()
                    drawingViewId = R.id.nav_host_fragment
                    setAllContainerColors(SurfaceColors.getColorForElevation(
                        requireContext(),
                        resources.getDimension(R.dimen.plane_01)
                    ))
                }
            }
            TransitionType.SHARED_AXIS_Y -> {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
            }
        }
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

    override fun onAudioPlayClicked(url: String?) {
        viewModel.onPlayAudioClicked(url)
    }

    override fun onAudioStopClicked() {
        viewModel.onStopAudioClicked()
    }

    override fun onAudioError(messageRes: Int) {
        viewModel.onAudioClipError(messageRes)
    }

    override fun onAddOnDetailsClicked(addOn: AddOn) {
        findNavController()
            .navigate(DetailsFragmentDirections.actionDetailsFragmentToAddOnsFragment(addOn))
    }

    override fun onAddOnDismissClicked(addOn: AddOn) {
        viewModel.onAddOnDismissClicked(addOn)
    }

    override fun onMwThesaurusChipClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
    }

    private fun showSnackbar(model: SnackbarModel) {
        model.make(binding.coordinatorLayout).show()
    }
}

