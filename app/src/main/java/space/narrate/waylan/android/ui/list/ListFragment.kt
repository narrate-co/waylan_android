package space.narrate.waylan.android.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentListBinding
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.widget.ElasticTransition
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

    // The MainViewModel used to share data between MainActivity and its child Fragments
    private val sharedViewModel: MainViewModel by sharedViewModel()

    // ListFragment's own ViewModel
    private val viewModel: ListViewModel by viewModel()

    private val adapter by lazy { ListItemAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setListType(navArgs<ListFragmentArgs>().value.listType)

        binding.run {

            appBar.doOnElasticDrag(
                alphaViews = listOf(recyclerView, appBar)
            )

            appBar.doOnElasticDismiss {
                navigator.toBack(Navigator.BackType.DRAG, this.javaClass.simpleName)
            }

            appBar.setOnNavigationIconClicked {
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }

            setUpList()

        }

        // Start enter transition now that things are set up.
        startPostponedEnterTransition()
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
                ContextCompat.getDrawable(context!!, R.drawable.list_item_divider)
            )
            recyclerView.addItemDecoration(itemDivider)

            viewModel.listType.observe(this@ListFragment) { type ->
                appBar.title = getString(type.titleRes)
            }
        }


        viewModel.list.observe(this) {
            adapter.submitList(it)
        }
    }

    override fun onWordClicked(word: String) {
        sharedViewModel.onChangeCurrentWord(word)
        findNavController().navigate(R.id.action_listFragment_to_detailsFragment)
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