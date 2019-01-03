package com.wordsdict.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wordsdict.android.MainViewModel
import com.wordsdict.android.Navigator
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.collapse
import com.wordsdict.android.util.expand
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contextual, container, false)
    }

    override fun onEnterTransactionEnded() {
        setUpSheet(view)

        chip2.setOnClickListener {
            sharedViewModel.setAppliedListFilter(listOf("month"))
        }
        close.setOnClickListener {
            sharedViewModel.setAppliedListFilter(emptyList())
        }
    }

    private fun setUpSheet(view: View?) {
        if (view == null) return

        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            when (dest) {
                Navigator.HomeDestination.LIST -> {
                    val hasAppliedFilter = sharedViewModel.appliedListFilter.value?.isNotEmpty() ?: false
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
            if (sharedViewModel.getBackStack().value?.peek() == Navigator.HomeDestination.LIST) {
                if (sharedViewModel.appliedListFilter.value?.isEmpty() == true) {
                    hide()
                } else {
                    peek()
                }
            } else {
                hide()
            }
        })
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