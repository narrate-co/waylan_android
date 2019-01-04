package com.wordsdict.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.wordsdict.android.MainActivity
import com.wordsdict.android.MainViewModel
import com.wordsdict.android.Navigator
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.collapse
import com.wordsdict.android.util.expand
import com.wordsdict.android.util.getScaleBetweenRange
import com.wordsdict.android.util.hide
import kotlinx.android.synthetic.main.fragment_contextual.*

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

    override fun onEnterTransactionEnded() {

        setUpSheet(view)

        close.setOnClickListener {
            sharedViewModel.setListFilter(emptyList())
        }
    }

    private fun setUpSheet(view: View?) {
        if (view == null) return

        setUpExpandedContainer()

        // When the current destination changes, update the sheet's state depending on
        // the new destination and whether or not it has an applied filter
        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            when (dest) {
                Navigator.HomeDestination.TRENDING -> {
                    // Do nothing. Let the current list filter observer handle peeking/hiding
                }
                else -> hide()
            }
        })

        // When the current list's filter changes, remove the collapsed bar's chips and replace
        // them with the new items (if any)
        sharedViewModel.getCurrentListFilterLive().observe(this, Observer {

            setSheetPeekable(it.isNotEmpty())

            replaceCollapsedContainerChips(it)

            if (sharedViewModel.getBackStack().value?.peek() == Navigator.HomeDestination.TRENDING) {
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
        (activity as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val peekBarAlpha = getScaleBetweenRange(offset, 0.0F, 0.5F, 1.0F, 0.0F)
            val expandedContainerAlpha = getScaleBetweenRange(offset, 0.5F, 1.0F, 0.0F, 1.0F)
            collapsedContainer.alpha = peekBarAlpha
            expandedContainer.alpha = expandedContainerAlpha
        }
    }

    private fun setUpExpandedContainer() {
        Period.values().forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    chipGroup,
                    false
            ) as Chip
            val label = getString(period.label)
            chip.text = label
            chip.setOnClickListener {
                sharedViewModel.setListFilter(listOf(period))
            }
            chipGroup.addView(chip)
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
        val dest = sharedViewModel.getBackStack().value?.peek() ?: Navigator.HomeDestination.HOME
        val hasAppliedFilter = sharedViewModel.getCurrentListFilter().isNotEmpty()

        // if we're at Trending and there is a non empty filter, don't allow the sheet to be hidden
        if (dest == Navigator.HomeDestination.TRENDING && hasAppliedFilter) {
            bottomSheetBehavior.isHideable = false
        } else {
            bottomSheetBehavior.isHideable = true
        }
    }

    fun expand() {
        bottomSheetBehavior.expand()
    }

    fun peek() {
        bottomSheetBehavior.collapse()
    }

    private fun hide() {
        setSheetHideable()
        bottomSheetBehavior.hide()
    }
}