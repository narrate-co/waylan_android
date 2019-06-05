package space.narrate.words.android.ui.third_party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.viewmodel.ext.android.sharedViewModel
import space.narrate.words.android.ui.MainViewModel
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseFragment
import space.narrate.words.android.ui.list.ListItemDividerDecoration
import space.narrate.words.android.Navigator
import space.narrate.words.android.data.prefs.ThirdPartyLibrary
import space.narrate.words.android.util.setUpWithElasticBehavior
import space.narrate.words.android.ui.widget.ElasticTransition

/**
 * A simple fragment that displays a static list of [ThirdPartyLibrary]
 */
class ThirdPartyLibrariesFragment : BaseFragment(), ThirdPartyLibraryAdapter.Listener {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var recyclerView: RecyclerView

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third_party_libraries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        appBarLayout = view.findViewById(R.id.app_bar)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        recyclerView = view.findViewById(R.id.recycler_view)

        appBarLayout.setUpWithElasticBehavior(
            this.javaClass.simpleName,
            sharedViewModel,
            listOf(navigationIcon),
            listOf(recyclerView, appBarLayout)
        )

        navigationIcon.setOnClickListener {
            sharedViewModel.onNavigationIconClicked(this.javaClass.simpleName)
        }

        setUpList()
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight
        )
        recyclerView.updatePadding(bottom = insets.systemWindowInsetBottom)
        return super.handleApplyWindowInsets(insets)
    }

    private fun setUpList() {
        val adapter = ThirdPartyLibraryAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val itemDivider = ListItemDividerDecoration(
            ContextCompat.getDrawable(requireContext(), R.drawable.list_item_divider)
        )
        recyclerView.addItemDecoration(itemDivider)

        adapter.submitList(sharedViewModel.thirdPartyLibraries)
    }

    override fun onClick(lib: ThirdPartyLibrary) {
        Navigator.launchWebsite(requireContext(), lib.url)
    }
}