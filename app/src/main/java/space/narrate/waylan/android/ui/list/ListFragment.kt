package space.narrate.waylan.android.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import space.narrate.waylan.android.*
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.android.ui.search.ContextualFragment
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.core.ui.widget.ListItemDividerDecoration
import space.narrate.waylan.core.util.MathUtils
import space.narrate.waylan.core.util.setUpWithElasticBehavior

/**
 * A flexible Fragment that handles the display of a [ListType]. Each [ListType] configuration is
 * essentially same, differing in only where it's data comes from and what this Fragments
 * AppBarLayout (faked) titleRes is set to.
 *
 */
class ListFragment: BaseFragment(), ListItemAdapter.ListItemListener {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbarContainer: ConstraintLayout
    private lateinit var toolbarTitle: AppCompatTextView
    private lateinit var toolbarTitleCollapsed: AppCompatTextView
    private lateinit var underline: View

    private val navigator: Navigator by inject()

    // The MainViewModel used to share data between MainActivity and its child Fragments
    private val sharedViewModel: MainViewModel by sharedViewModel()

    // ListFragment's own ViewModel
    private val viewModel: ListViewModel by viewModel()

    private val adapter by lazy { ListItemAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Postpone enter transition until we've set everything up.
        postponeEnterTransition()

        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        appBarLayout = view.findViewById(R.id.app_bar)
        recyclerView = view.findViewById(R.id.recycler_view)
        toolbarContainer = view.findViewById(R.id.toolbar_container)
        toolbarTitle = view.findViewById(R.id.toolbar_title)
        toolbarTitleCollapsed = view.findViewById(R.id.toolbar_title_collapsed)
        underline = view.findViewById(R.id.underline)

        viewModel.setListType(navArgs<ListFragmentArgs>().value.listType)

        appBarLayout.setUpWithElasticBehavior(
            this.javaClass.simpleName,
            navigator,
            listOf(appBarLayout),
            listOf(recyclerView, appBarLayout)
        )

        // Set the toolbarTitle according to the above listType
        navigationIcon.setOnClickListener {
            // Child fragments of MainActivity should report how the user is navigating away
            // from them. For more info, see [BaseFragment.setUnconsumedNavigationMethod]
            navigator.back(Navigator.BackType.ICON, this.javaClass.simpleName)
        }

        // Set up expanding/collapsing "toolbar"
        setUpReachabilityAppBar()

        setUpList()

        // Start enter transition now that things are set up.
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight
        )
        recyclerView.updatePadding(
            bottom = ContextualFragment.getPeekHeight(requireContext(), insets)
        )
        return super.handleApplyWindowInsets(insets)
    }

    private fun setUpList() {
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

        viewModel.listType.observe(this, Observer {
            toolbarTitle.text = getString(it.titleRes)
            toolbarTitleCollapsed.text = getString(it.titleRes)
        })

        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    /**
     * If the list is empty or the user has not seen the banner (which acts as an onboarding
     * mechanism to describe the contents of this instance's [ListType], set the adapter's
     * header
     */


    /**
     * Manually translates views in this Fragment's AppBarLayout to create an expanding/collapsing
     * toolbar.
     */
    private fun setUpReachabilityAppBar() {

        appBarLayout.doOnPreDraw {
            // TODO set height of expanded toolbar based on view height. Set collapsed if
            // under a certain limit

            // set min height
            val minCollapsedHeight = underline.bottom - navigationIcon.top
            val toolbarTitleCollapsedHeight = toolbarTitleCollapsed.height

            val alphaFraction = 0.6F
            toolbarContainer.minimumHeight = minCollapsedHeight
            appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    val totalScrollRange = appBarLayout.totalScrollRange - minCollapsedHeight
                    val interpolationEarlyFinish = Math.abs(
                        verticalOffset.toFloat()
                    ) / totalScrollRange

                    // translate the navIcon to make room for the collapsed toolbar titleRes
                    val navIconTransY =
                        (1 - interpolationEarlyFinish) * toolbarTitleCollapsedHeight
                    navigationIcon.translationY = navIconTransY

                    // hide/show the collapsed toolbar titleRes
                    val offsetInterpolation = MathUtils.normalize(
                        interpolationEarlyFinish,
                        alphaFraction,
                        1F,
                        0F,
                        1F
                    )
                    toolbarTitleCollapsed.alpha = offsetInterpolation
                })
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