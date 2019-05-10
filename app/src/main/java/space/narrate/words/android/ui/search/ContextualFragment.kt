package space.narrate.words.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import space.narrate.words.android.MainActivity
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.Navigator
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*
import kotlinx.android.synthetic.main.fragment_contextual.*

/**
 * A second bottom sheet that lives behind the SearchFragment sheet. This is used to give
 * secondary content about what is in the main fragment container. This Fragment presents
 * things like a filter for filtering a list.
 */
class ContextualFragment : BaseUserFragment() {

    companion object {
        fun newInstance() = ContextualFragment()
        const val TAG = "ContextualFragment"
    }

    // MainViewModel owned by MainActivity and used to share data between MainActivity
    // and its child Fragments
    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    // The BottomSheetBehavior of this view.
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contextual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSheet(view)

        close.setOnClickListener {
            sharedViewModel.setListFilter(emptyList())
        }

        sharedViewModel.shouldOpenContextualSheet.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { expand() }
        })
    }


    private fun setUpSheet(view: View?) {
        if (view == null) return

        setUpExpandedContainer("Filter")

        // When the current destination changes, update the sheet's state
        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = it.peekOrNull ?: Navigator.HomeDestination.HOME
            when (dest) {
                Navigator.HomeDestination.TRENDING -> {
                    // Set the expanded sheet up for trending
                    setUpExpandedContainer("Filter trending")
                }
                else -> hide()
            }
        })

        // When the current list's filter changes, remove the collapsed bar's chips and replace
        // them with the new items (if any)
        sharedViewModel.getCurrentListFilterLive().observe(this, Observer {

            setSheetPeekable(it.isNotEmpty())

            replaceCollapsedContainerChips(it)

            if (sharedViewModel.getBackStack().value?.peekOrNull == Navigator.HomeDestination.TRENDING) {
                if (it.isEmpty()) {
                    hide()
                } else {
                    peek()
                }
            } else {
                hide()
            }
        })

        // Animate the switching of containers between the collapsed (peeked) state and the
        // expanded state. Peeking should just show the top collapsedContainer and the expanded
        // state should just show the expandedContainer
        (requireActivity() as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val peekBarAlpha = MathUtils.normalize(offset, 0.0F, 0.5F, 1.0F, 0.0F)
            val expandedContainerAlpha = MathUtils.normalize(offset, 0.5F, 1.0F, 0.0F, 1.0F)
            collapsed_container.alpha = peekBarAlpha
            expandedContainer.alpha = expandedContainerAlpha
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set default values before observables emit values.
        setSheetPeekable(false)
    }

    private fun setUpExpandedContainer(title: String) {
        this.title.text = title
        chip_group.removeAllViews()
        Period.values().forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    chip_group,
                    false
            ) as Chip
            val label = getString(period.label)
            chip.text = label
            chip.setOnClickListener {
                sharedViewModel.setListFilter(listOf(period))
            }
            chip_group.addView(chip)
        }
    }

    private fun replaceCollapsedContainerChips(list: List<Period>) {
        collapsedChipGroup.removeAllViews()
        list.forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    collapsedChipGroup,
                    false
            ) as Chip
            chip.text = getString(period.label)
            collapsedChipGroup.addView(chip)
        }
    }

    private fun setSheetPeekable(peekable: Boolean) {
        if (peekable) {
            bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.contextual_min_peek_height)
        } else {
            bottomSheetBehavior.peekHeight = 0
        }
    }

    /**
     * Determine whether the sheet should be able to be hidden or not depending on the current
     * HomeDestination and whether or not we have a filter applied for it.
     */
    private fun setSheetHideable() {
        val dest = sharedViewModel.getBackStack().value?.peekOrNull ?: Navigator.HomeDestination.HOME
        val hasAppliedFilter = sharedViewModel.getCurrentListFilter().isNotEmpty()

        // if we're at Trending and there is a non empty filter, don't allow the sheet to be hidden
        if (dest == Navigator.HomeDestination.TRENDING && hasAppliedFilter) {
            bottomSheetBehavior.isHideable = false
        } else {
            bottomSheetBehavior.isHideable = true
        }
    }

    private fun expand() {
        bottomSheetBehavior.expand()
    }

    private fun peek() {
        bottomSheetBehavior.collapse()
    }

    private fun hide() {
        setSheetHideable()
        bottomSheetBehavior.hide()
    }
}