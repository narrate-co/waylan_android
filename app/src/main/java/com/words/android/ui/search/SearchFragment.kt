package com.words.android.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.textservice.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.*
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.hideSoftKeyboard
import android.view.textservice.SuggestionsInfo
import kotlinx.android.synthetic.main.search_fragment.*
import kotlinx.android.synthetic.main.search_fragment.view.*


class SearchFragment : BaseUserFragment(), WordsAdapter.WordAdapterHandlers, TextWatcher {



    companion object {
        fun newInstance() = SearchFragment()
        const val NOT_A_LENGTH = -1
        const val TAG = "SearchFragment"
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SearchViewModel::class.java)
    }

    private val sharedViewHolder by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    private val adapter by lazy { WordsAdapter(this) }

    var spellCheckerSession: SpellCheckerSession? = null

    private var hideKeyboard = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.search_fragment, container, false)

        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

        view.searchEditText.addTextChangedListener(this)

        view.searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        view.searchEditText.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
        })


        return view
    }

    override fun onStop() {
        super.onStop()
        spellCheckerSession?.close()
        spellCheckerSession = null
    }

    override fun onWordClicked(word: String) {
        sharedViewHolder.setCurrentWordId(word)
        hideKeyboard = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        activity?.hideSoftKeyboard()
        (activity as MainActivity).showDetails()
    }


    private fun dumpSuggestionsInfoInternal(
            sb: StringBuilder, si: SuggestionsInfo, length: Int, offset: Int) {
        // Returned suggestions are contained in SuggestionsInfo
        val len = si.suggestionsCount
        sb.append('\n')
        for (j in 0 until len) {
            if (j != 0) {
                sb.append(", ")
            }
            sb.append(si.getSuggestionAt(j))
        }
        sb.append(" ($len)")
        if (length != NOT_A_LENGTH) {
            sb.append(" length = $length, offset = $offset")
        }

    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchInput = s?.toString() ?: ""
    }
}

