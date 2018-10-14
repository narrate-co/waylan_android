package com.words.android.ui.details

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.databinding.DetailsFragmentBinding
import com.words.android.data.firestore.UserWord
import com.words.android.data.firestore.UserWordType
import com.words.android.data.repository.Word
import com.words.android.ui.common.BaseUserFragment
import kotlinx.android.synthetic.main.details_fragment.*

class DetailsFragment: BaseUserFragment(), Toolbar.OnMenuItemClickListener, DetailsAdapter.Listener {

    companion object {
        const val FRAGMENT_TAG = "details_fragment_tag"
        fun newInstance() = DetailsFragment()
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private var currentWordValue: Word = Word()

    private val adapter: DetailsAdapter by lazy { DetailsAdapter(this) }

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
        setUpRecyclerView()
        sharedViewModel.currentWord.observe(this, Observer {
            sharedViewModel.setCurrentWordRecented()
            setUserWord(it?.userWord)
            adapter.submitWord(it)
        })

    }

    private fun setUpRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView?.adapter = adapter
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

    override fun onRelatedWordClicked(relatedWord: String) {
        sharedViewModel.setCurrentWordId(relatedWord)
    }


    override fun onSynonymChipClicked(synonym: String) {
        sharedViewModel.setCurrentWordId(synonym)
    }

    override fun onAudioClipError(message: String) {
        //TODO show a Snackbar
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null || currentWordValue.userWord == userWord) return
        currentWordValue.userWord = userWord

        println("setUserWord - userWord = $userWord")
        val favoriteMenuItem = toolbar.menu?.findItem(R.id.action_favorite)
        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)
        favoriteMenuItem?.isChecked = isFavorited
        favoriteMenuItem?.icon = ContextCompat.getDrawable(context!!, if (isFavorited) R.drawable.ic_round_favorite_24px else R.drawable.ic_round_favorite_border_24px)

    }

}

