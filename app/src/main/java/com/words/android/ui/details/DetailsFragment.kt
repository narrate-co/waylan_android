package com.words.android.ui.details

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.words.android.App
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.data.disk.wordset.Example
import com.words.android.data.disk.wordset.Meaning
import com.words.android.databinding.DetailsFragmentBinding
import com.words.android.data.firestore.UserWord
import com.words.android.data.firestore.UserWordType
import com.words.android.util.toChip
import kotlinx.android.synthetic.main.details_fragment.*
import kotlinx.android.synthetic.main.details_fragment.view.*

class DetailsFragment: Fragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private val sharedViewModel by lazy {
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

        sharedViewModel.currentWord.observe(this, Observer {
            println("currentWordChanged!")
            sharedViewModel.setCurrentWordRecented()
            setMeanings(it?.dbMeanings)
            setUserWord(it?.userWord)
        })
        return binding.root
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        //TODO finish implementation
        println("menu item clicked")
        return when (item?.itemId) {
            R.id.action_favorite -> {
                val isChecked = item.isChecked
                println("menu item favorite clicked. is Checked = $isChecked")
                sharedViewModel.setCurrentWordFavorited(!item.isChecked)
                true
            }
            else -> false
        }
    }


    private fun createDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, view?.definitionsLinearLayout, false) as AppCompatTextView
        textView.text = ": $def"
        return textView
    }

    private fun createExampleView(example: Example): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_example_layout, view?.examplesLinearLayout, false) as AppCompatTextView
        textView.text = example.example
        return textView
    }

    private fun setMeanings(meanings: List<Meaning>?) {
        if (meanings == null) return

        //remove all views
        view?.definitionsLinearLayout?.removeAllViews()
        view?.chipGroup?.removeAllViews()
        view?.examplesLinearLayout?.removeAllViews()

        //set part of speech
        val posMap = meanings.map { it.partOfSpeech }.distinct()
        if (posMap.isNotEmpty()) {
            view?.partOfSpeechTextView?.text = posMap.reduce { acc, s -> "$acc | $s" }
        }

        //add definitions
        meanings.forEach {
            view?.definitionsLinearLayout?.addView(createDefinitionView(it.def))
        }

        //add synonyms
        meanings.map { it.synonyms }.flatten().forEach {
            view?.chipGroup?.addView(it.toChip(context!!, view?.chipGroup) {
                sharedViewModel.setCurrentWordId(it.synonym)
            })
        }

        //add examples
        meanings.map { it.examples }.flatten().forEach {
            view?.examplesLinearLayout?.addView(createExampleView(it))
        }

    }

    private fun setUserWord(userWord: UserWord?) {
        println("setUserWord - userWord = $userWord")
        val favoriteMenuItem = toolbar.menu?.findItem(R.id.action_favorite)
        val isFavorited = userWord?.types?.containsKey(UserWordType.FAVORITED.name) ?: false
        favoriteMenuItem?.isChecked = isFavorited
        favoriteMenuItem?.icon = ContextCompat.getDrawable(context!!, if (isFavorited) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)

    }

}

