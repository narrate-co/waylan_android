package space.narrate.waylan.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentHomeBinding

/**
 * The fragment displaying the main menu list of possible destinations: Trending, Recents, Favorites
 * and Settings. The List destinations (Trending, Recents, Favorites) all contain previews of
 * their list.
 */
class HomeFragment: Fragment(), HomeItemAdapter.HomeItemListener {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: HomeViewModel by viewModel()

    private val adapter by lazy { HomeItemAdapter(this) }

    // Has entered is used to determine whether or not this Fragment is being returned to. Since
    // this member variable will be retained between recreations of this fragments view, it should
    // be "stateful" across navigation events.
    private var hasEntered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.artView.animateSmear(!hasEntered)
        hasEntered = true;
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                true
            )
            recyclerView.adapter = adapter
        }

        viewModel.list.observe(this) {
            adapter.submitList(it)
        }
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