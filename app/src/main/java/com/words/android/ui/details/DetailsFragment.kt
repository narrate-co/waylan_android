package com.words.android.ui.details

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.words.android.WFragment
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.data.disk.wordset.Example
import com.words.android.data.disk.wordset.Meaning
import com.words.android.databinding.DetailsFragmentBinding
import com.words.android.data.firestore.UserWord
import com.words.android.data.firestore.UserWordType
import com.words.android.data.repository.Word
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.toChip
import kotlinx.android.synthetic.main.details_fragment.*
import kotlinx.android.synthetic.main.details_fragment.view.*

class DetailsFragment: BaseUserFragment(), Toolbar.OnMenuItemClickListener, MerriamWebsterDefinitionsView.MerriamWebsterViewListener {

    companion object {
        private const val TAG = "DetailsFragment"
        const val FRAGMENT_TAG = "details_fragment_tag"
        fun newInstance() = DetailsFragment()
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private var currentWordValue: Word = Word()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: DetailsFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.details_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.sharedViewModel = sharedViewModel
        binding.toolbar.inflateMenu(R.menu.details_menu)
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.toolbar.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        return binding.root
    }

    // defer load intensive work until after the fragment transaction has ended
    override fun onEnterTransactionEnded() {
        sharedViewModel.currentWord.observe(this, Observer {
            sharedViewModel.setCurrentWordRecented()
            setMeanings(it?.dbMeanings)
            setMerriamWebster(it?.mwEntry)
            setUserWord(it?.userWord)
        })
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_favorite -> {
                sharedViewModel.setCurrentWordFavorited(!item.isChecked)
                true
            }
            //TODO implement share?
            else -> false
        }
    }

    override fun onRelatedWordClicked(word: String) {
        sharedViewModel.setCurrentWordId(word)
    }

    private fun setMerriamWebster(entry: List<WordAndDefinitions>?) {
        //TODO handle words w/o MW Entries!
        println("$TAG::setMerriamWebster - LAST: ${currentWordValue.mwEntry} | NEW: $entry")

        view?.merriamDefinitionsLinearLayout?.setWordAndDefinitions(entry)

        currentWordValue.mwEntry = entry  ?: emptyList()

    }

    private fun createPartOfSpeechView(pos: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_part_of_speech_layout, view?.definitionsLinearLayout, false) as AppCompatTextView
        textView.text = pos
        return textView
    }

    private fun createDefinitionView(def: String): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_definition_layout, view?.definitionsLinearLayout, false) as AppCompatTextView
        textView.text = ":$def"
        return textView
    }

    private fun createExampleView(example: Example): AppCompatTextView {
        val textView: AppCompatTextView = LayoutInflater.from(context).inflate(R.layout.details_example_layout, view?.examplesLinearLayout, false) as AppCompatTextView
        textView.text = example.example
        return textView
    }

    private fun setMeanings(meanings: List<Meaning>?) {
        if (meanings == null || meanings == currentWordValue.dbMeanings) return
        currentWordValue.dbMeanings = meanings

        //remove all views
        view?.definitionsLinearLayout?.removeAllViews()
        view?.chipGroup?.removeAllViews()
        view?.examplesLinearLayout?.removeAllViews()

        //add definition groups for each partOfSpeech
        meanings.groupBy { it.partOfSpeech }.entries.forEach {
            view?.definitionsLinearLayout?.addView(createPartOfSpeechView(it.key))
            it.value.forEach {
                view?.definitionsLinearLayout?.addView(createDefinitionView(it.def))
            }
        }

        //add synonyms
        meanings.map { it.synonyms }.flatten().forEach {
            view?.chipGroup?.addView(it.toChip(context!!, view?.chipGroup) {
                sharedViewModel.setCurrentWordId(it.synonym)
            })
        }

        //add examples
        val examples = meanings.map { it.examples }
        if (examples.isNotEmpty()) {
            examples.flatten().forEach {
                view?.examplesLinearLayout?.addView(createExampleView(it))
            }
        }


    }

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null || currentWordValue.userWord == userWord) return
        currentWordValue.userWord = userWord

        println("setUserWord - userWord = $userWord")
        val favoriteMenuItem = toolbar.menu?.findItem(R.id.action_favorite)
        val isFavorited = userWord?.types?.containsKey(UserWordType.FAVORITED.name) ?: false
        favoriteMenuItem?.isChecked = isFavorited
        favoriteMenuItem?.icon = ContextCompat.getDrawable(context!!, if (isFavorited) R.drawable.ic_round_favorite_24px else R.drawable.ic_round_favorite_border_24px)

    }

}

