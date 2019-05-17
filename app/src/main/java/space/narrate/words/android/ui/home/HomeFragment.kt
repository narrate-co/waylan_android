package space.narrate.words.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import space.narrate.words.android.MainActivity
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.ui.list.ListFragmentDirections
import space.narrate.words.android.ui.list.ListType
import space.narrate.words.android.util.widget.ElasticTransition


/**
 * The fragment displaying the main menu list of possible destinations: Trending, Recents, Favorites
 * and Settings. The List destinations (Trending, Recents, Favorites) all contain previews of
 * their list.
 */
class HomeFragment: BaseUserFragment() {

    private lateinit var trendingContainer: LinearLayout
    private lateinit var recentContainer: LinearLayout
    private lateinit var favoriteContainer: LinearLayout
    private lateinit var settingsTextView: AppCompatTextView
    private lateinit var scrollContainer: NestedScrollView
    private lateinit var constraintContainer: ConstraintLayout
    private lateinit var statusBarScrimView: View
    private lateinit var trendingPreviewTextView: AppCompatTextView
    private lateinit var recentPreviewTextView: AppCompatTextView
    private lateinit var favoritePreviewTextView: AppCompatTextView


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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trendingContainer = view.findViewById(R.id.trending_container)
        recentContainer = view.findViewById(R.id.recent_container)
        favoriteContainer = view.findViewById(R.id.favorite_container)
        settingsTextView = view.findViewById(R.id.settings_text_view)
        scrollContainer = view.findViewById(R.id.scroll_container)
        constraintContainer = view.findViewById(R.id.constraint_container)
        statusBarScrimView = view.findViewById(R.id.status_bar_scrim)
        trendingPreviewTextView = view.findViewById(R.id.trending_preview_text_view)
        recentPreviewTextView = view.findViewById(R.id.recent_preview_text_view)
        favoritePreviewTextView = view.findViewById(R.id.favorite_preview_text_view)

        // Set menu on click listeners
        trendingContainer.setOnClickListener {
            onMenuButtonClicked(ListType.TRENDING)
        }
        recentContainer.setOnClickListener {
            onMenuButtonClicked(ListType.RECENT)
        }
        favoriteContainer.setOnClickListener {
            onMenuButtonClicked(ListType.FAVORITE)
        }
        settingsTextView.setOnClickListener {
            launchSettings()
        }

        setUpReachabilityParams(view)

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
            val totalHeight = scrollContainer.height
            val menuContainerHeight = constraintContainer.height
            val topOffset = Math.max(
                statusBarScrimView.height,
                totalHeight - menuContainerHeight - resources.getDimensionPixelSize(R.dimen.search_min_peek_height) - resources.getDimensionPixelSize(R.dimen.home_menu_bottom_offset_min)
            )
            scrollContainer.updatePadding(top = topOffset)
        }
    }

    /**
     * Each list menu item contains 0-4 items as a preview for what that menu destination
     * contains. This functions sets those previews up by observing each list's data source.
     */
    private fun setUpListPreviews() {
        viewModel.getPreview(ListType.TRENDING).observe(this, Observer {
            trendingPreviewTextView.text = it
        })
        viewModel.getPreview(ListType.RECENT).observe(this, Observer {
            recentPreviewTextView.text = it
        })
        viewModel.getPreview(ListType.FAVORITE).observe(this, Observer {
            favoritePreviewTextView.text = it
        })
    }

    private fun onMenuButtonClicked(type: ListType) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToListFragment(type)
        )
    }

    private fun launchSettings() {
        findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
    }
}