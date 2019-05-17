package space.narrate.words.android.ui.settings

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
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.ui.list.ListItemDividerDecoration
import space.narrate.words.android.Navigator
import space.narrate.words.android.util.widget.ElasticTransition

/**
 * A simple fragment that displays a static list of [ThirdPartyLibrary]
 */
class ThirdPartyLibrariesFragment : BaseUserFragment(), ThirdPartyLibraryAdapter.Listener {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var recyclerView: RecyclerView

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
        navigationIcon = view.findViewById(R.id.navigation_icon)
        recyclerView = view.findViewById(R.id.recycler_view)

        navigationIcon.setOnClickListener {
            requireActivity().onBackPressed()
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

        adapter.submitList(ThirdPartyLibraryStore.ALL)
    }

    override fun onClick(lib: ThirdPartyLibrary) {
        Navigator.launchWebsite(context!!, lib.url)
    }
}