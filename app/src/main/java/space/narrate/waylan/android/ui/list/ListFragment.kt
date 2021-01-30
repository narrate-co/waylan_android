package space.narrate.waylan.android.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import java.util.concurrent.TimeUnit
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentListBinding
import space.narrate.waylan.android.ui.MainActivity
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.ui.details.DetailsFragmentDirections
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.TransitionType
import space.narrate.waylan.core.ui.widget.ListItemDividerDecoration

/**
 * A flexible Fragment that handles the display of a [ListType]. Each [ListType] configuration is
 * essentially same, differing in only where it's data comes from and what this Fragments
 * AppBarLayout (faked) titleRes is set to.
 *
 */
class ListFragment: Fragment(), ListItemAdapter.ListItemListener {

    private lateinit var binding: FragmentListBinding

    private val navigator: Navigator by inject()

    private val args: ListFragmentArgs by lazy { navArgs<ListFragmentArgs>().value }

    // The MainViewModel used to share data between MainActivity and its child Fragments
    private val sharedViewModel: MainViewModel by sharedViewModel()

    // ListFragment's own ViewModel
    private val viewModel: ListViewModel by viewModel()

    private val adapter by lazy { ListItemAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = args.transitionForward
        val transitionType = args.transitionType
        setUpTransitions(transitionType, forward)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postponeEnterTransition(500L, TimeUnit.MILLISECONDS)
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setListType(args.listType)

        setUpList()

        // Start enter transition now that things are set up.
    }

    fun setUpTransitions(type: TransitionType, forward: Boolean) {
        when (type) {
            TransitionType.SHARED_AXIS_X, TransitionType.SHARED_AXIS_Y -> {
                val axis = if (type == TransitionType.SHARED_AXIS_X) {
                    MaterialSharedAxis.X
                } else {
                    MaterialSharedAxis.Y
                }
                enterTransition = MaterialSharedAxis(axis, forward)
                returnTransition = MaterialSharedAxis(axis, !forward)

                exitTransition = MaterialSharedAxis(axis, forward)
                reenterTransition = MaterialSharedAxis(axis, !forward)
            }
          TransitionType.FADE_THROUGH -> {
              enterTransition = MaterialFadeThrough()
              returnTransition = MaterialFadeThrough()
              exitTransition = MaterialFadeThrough()
              reenterTransition = MaterialFadeThrough()
          }
        }
    }

    private fun setUpList() {
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            recyclerView.adapter = adapter
            val itemDivider = ListItemDividerDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.list_item_divider)
            )
            recyclerView.addItemDecoration(itemDivider)

            viewModel.listType.observe(this@ListFragment.viewLifecycleOwner) { type ->
                appBar.title = getString(type.titleRes)
            }

            // Wait for the recycler to draw it's children so they can be found by the shared
            // element transition
            recyclerView.doOnNextLayout { startPostponedEnterTransition() }
        }

        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onWordClicked(word: String, view: View, useSharedElement: Boolean) {
        exitTransition = Hold()
        reenterTransition = null
        sharedViewModel.onChangeCurrentWord(word)
        if (useSharedElement) {
            val extras = FragmentNavigatorExtras(view to "details_container_transition_group")
            findNavController().navigate(
                R.id.action_listFragment_to_detailsFragment,
                null,
                null,
                extras
            )
        } else {
            val mainActivity = (requireActivity() as MainActivity)
            (mainActivity.currentNavigationFragment as? ListFragment)?.apply {
                setUpTransitions(TransitionType.SHARED_AXIS_Y, true)
            }
            findNavController().navigate(
                DetailsFragmentDirections.actionGlobalDetailsFragment(TransitionType.SHARED_AXIS_Y)
            )
        }

    }

    override fun onBannerClicked() {
        // do nothing
    }

    override fun onBannerLabelClicked() {
        // do nothing
    }

    override fun onBannerTopButtonClicked() {
        sharedViewModel.onShouldOpenAndFocusSearch()
    }

    override fun onBannerBottomButtonClicked() {
        viewModel.onBannerDismissClicked()
    }
}