package space.narrate.words.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import space.narrate.words.android.MainActivity
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.ui.list.ListFragment
import kotlinx.android.synthetic.main.fragment_home.view.*


/**
 * The fragment displaying the main menu list of possible destinations: Trending, Recents, Favorites
 * and Settings. The List destinations (Trending, Recents, Favorites) all contain previews of
 * their list.
 */
class HomeFragment: BaseUserFragment() {

    companion object {
        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "home_fragment_tag"

        fun newInstance() = HomeFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(HomeViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Set menu on click listeners
        view.trendingContainer.setOnClickListener {
            onMenuButtonClicked(ListFragment.ListType.TRENDING)
        }
        view.recentContainer.setOnClickListener {
            onMenuButtonClicked(ListFragment.ListType.RECENT)
        }
        view.favoriteContainer.setOnClickListener {
            onMenuButtonClicked(ListFragment.ListType.FAVORITE)
        }
        view.settings.setOnClickListener { launchSettings() }

        setUpReachabilityParams(view)
        return view
    }

    // Defer load intensive work until after enter transaction has ended
    override fun onEnterTransactionEnded() {
        setUpListPreviews()
    }

    /**
     * Manually push down the entire containing view of this fragments menu layout (the Trending,
     * Recents, Favorites and Settings buttons). This is part of the Words UX in an effort to
     * have elements in more "reachable" positions. If half of the screen is going to be empty,
     * why not make it the top which is hard to reach?
     *
     * This method calculates the height of the main menu container, the height of the screen
     * and places the menu container just above the [SearchFragment].
     */
    private fun setUpReachabilityParams(view: View) {
        view.doOnPreDraw {
            val totalHeight = view.scrollContainer.height
            val menuContainerHeight = view.constraintContainer.height
            val topOffset = Math.max(
                    view.statusBarScrim.height,
                    totalHeight - menuContainerHeight - resources.getDimensionPixelSize(R.dimen.search_min_peek_height) - resources.getDimensionPixelSize(R.dimen.home_menu_bottom_offset_min)
            )
            println("HomeFragment::setUpReachabilityParams - totalHeight $totalHeight, menuContainerHeight = $menuContainerHeight, topOffset = $topOffset")
            view.scrollContainer.updatePadding(top = topOffset)
        }
    }

    /**
     * Each list menu item contains 0-4 items as a preview for what that menu destination
     * contains. This functions sets those previews up by observing each list's data source.
     */
    private fun setUpListPreviews() {
        viewModel.getListPreview(ListFragment.ListType.TRENDING).observe(this, Observer {
            view?.trendingPreview?.text = it
        })
        viewModel.getListPreview(ListFragment.ListType.RECENT).observe(this, Observer {
            view?.recentPreview?.text = it
        })
        viewModel.getListPreview(ListFragment.ListType.FAVORITE).observe(this, Observer {
            view?.favoritePreview?.text = it
        })
    }

    private fun onMenuButtonClicked(type: ListFragment.ListType) {
        (activity as? MainActivity)?.showListFragment(type)
    }

    private fun launchSettings() {
        (activity as? MainActivity)?.launchSettings()
    }
}