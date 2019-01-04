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
            sharedViewModel.setAppliedListFilter(emptyList())
        }
    }

    private fun setUpSheet(view: View?) {
        if (view == null) return

        Period.values().forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    chipGroup,
                    false
            ) as Chip
            val label = getString(period.label)
            chip.text = label
            chip.setOnClickListener {
                sharedViewModel.setAppliedListFilter(listOf(period))
            }
            chipGroup.addView(chip)
        }

        sharedViewModel.getBackStack().observe(this, Observer {
            setSheetHideable()
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            when (dest) {
                Navigator.HomeDestination.TRENDING -> {
                    val hasAppliedFilter =
                            sharedViewModel.appliedListFilter.value?.isNotEmpty() ?: false
                    if (hasAppliedFilter) {
                        peek()
                    } else {
                        hide()
                    }
                }
                else -> hide()
            }
        })

        sharedViewModel.appliedListFilter.observe(this, Observer {
            setSheetHideable()
            peekChipGroup.removeAllViews()
            if (it.isEmpty()) {
                bottomSheetBehavior.peekHeight = 0
            } else {
                it.forEach { period ->
                    val chip = LayoutInflater.from(context).inflate(
                            R.layout.contextual_chip_layout,
                            peekChipGroup,
                            false
                    ) as Chip
                    chip.text = getString(period.label)
                    peekChipGroup.addView(chip)
                }
                bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.contextual_min_peek_height)
            }

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

        (activity as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val peekBarAlpha =
                    getScaleBetweenRange(offset, 0.0F, 0.5F, 1.0F, 0.0F)
            val expandedContainerAlpha =
                    getScaleBetweenRange(offset, 0.5F, 1.0F, 0.0F, 1.0F)
            peekBarContainer.alpha = peekBarAlpha
            expandedContainer.alpha = expandedContainerAlpha
        }
    }

    private fun setSheetHideable() {
        val dest = sharedViewModel.getBackStack().value?.peek() ?: Navigator.HomeDestination.HOME
        val hasAppliedFilter = sharedViewModel.appliedListFilter.value?.isNotEmpty() == true

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
        bottomSheetBehavior.hide()
    }
}