package com.words.android.ui.details

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.words.android.App
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.data.Example
import com.words.android.data.Meaning
import com.words.android.data.Synonym
import com.words.android.databinding.DetailsFragmentBinding
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.details_fragment.view.*

class DetailsFragment: Fragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    val sharedViewModel by lazy {
        ViewModelProviders
                .of(activity!!, (activity!!.application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: DetailsFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.details_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.sharedViewModel = sharedViewModel
        binding.toolbar.inflateMenu(R.menu.details_menu)
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.toolbar.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        sharedViewModel.getCurrentMeanings().observe(this, Observer {
            if (it != null) {
                setMeanings(it)
            }
        })
        return binding.root
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        //TODO finish implementation
        println("menu item clicked")
        return true
    }


    private fun createDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, view?.definitionsLinearLayout, false) as AppCompatTextView
        textView.text = ": $def"
        return textView
    }

    private fun createSynonymChip(synonym: Synonym): Chip {
        val chip: Chip = LayoutInflater.from(context).inflate(R.layout.details_chip_layout, view?.chipGroup, false) as Chip
        chip.chipText = synonym.synonym
        chip.setOnClickListener {
            sharedViewModel.setCurrentWord(synonym.synonym)
        }
        return chip
    }

    private fun createExampleView(example: Example): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_example_layout, view?.examplesLinearLayout, false) as AppCompatTextView
        textView.text = example.example
        return textView
    }

    private fun setMeanings(meanings: List<Meaning>) {

        //remove all views
        view?.definitionsLinearLayout?.removeAllViews()
        view?.chipGroup?.removeAllViews()
        view?.examplesLinearLayout?.removeAllViews()

        //set part of speech
        view?.partOfSpeechTextView?.text = meanings.map { it.partOfSpeech }.distinct().reduce { acc, s -> "$acc | $s" }

        //add definitions
        meanings.forEach {
            view?.definitionsLinearLayout?.addView(createDefinitionView(it.def))
        }

        //add synonyms
        meanings.map { it.synonyms }.flatten().forEach {
            view?.chipGroup?.addView(createSynonymChip(it))
        }

        //add examples
        meanings.map { it.examples }.flatten().forEach {
            view?.examplesLinearLayout?.addView(createExampleView(it))
        }

    }

}

