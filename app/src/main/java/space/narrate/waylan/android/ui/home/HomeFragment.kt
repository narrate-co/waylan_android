package space.narrate.waylan.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.android.ui.search.SearchFragment

/**
 * The fragment displaying the main menu list of possible destinations: Trending, Recents, Favorites
 * and Settings. The List destinations (Trending, Recents, Favorites) all contain previews of
 * their list.
 */
class HomeFragment: BaseFragment(), HomeItemAdapter.HomeItemListener {

    private lateinit var statusBarScrimView: View
    private lateinit var recyclerView: RecyclerView

    private val viewModel: HomeViewModel by viewModel()

    private val adapter by lazy { HomeItemAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusBarScrimView = view.findViewById(R.id.status_bar_scrim)
        recyclerView = view.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            true
        )
        recyclerView.adapter = adapter

        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })

    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        recyclerView.updatePadding(
            top = insets.systemWindowInsetTop,
            bottom = SearchFragment.getPeekHeight(requireContext(), insets) +
                resources.getDimensionPixelSize(R.dimen.keyline_3)
        )
        return super.handleApplyWindowInsets(insets)
    }

    override fun onItemClicked(item: HomeItemModel.ItemModel) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToListFragment(item.listType)
        )
    }

    override fun onSettingsClicked() {
        findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
    }
}