package com.words.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.App
import com.words.android.MainActivity
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.databinding.SearchFragmentBinding
import com.words.android.data.repository.Word
import com.words.android.util.hideSoftKeyboard
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


class SearchFragment : Fragment(), WordsAdapter.WordAdapterHandlers {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, (activity?.application as App).viewModelFactory)
                .get(SearchViewModel::class.java)
    }

    private val sharedViewHolder by lazy {
        ViewModelProviders
                .of(activity!!, (activity?.application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    private val adapter by lazy { WordsAdapter(this) }

    private var hideKeyboard = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: SearchFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        //set up recycler view
        binding.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recycler.adapter = adapter

        binding.searchEditText.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        viewModel.searchResults.observe(this, Observer {
            if (it != null) {
                println("SearchFragment::search results changed. size = ${it.size}")
                adapter.submitList(it)
            }
        })

        return binding.root
    }

    override fun onWordClicked(word: Word) {
        println("SearchFragment::onWordClicked dbWord: ${word.dbWord?.word}, userWord: ${word.userWord?.word}")
        sharedViewHolder.setCurrentWordId(word.dbWord?.word ?: word.userWord?.word ?: "")
        hideKeyboard = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        activity?.hideSoftKeyboard()
        (activity as MainActivity).showDetails()
    }

}

