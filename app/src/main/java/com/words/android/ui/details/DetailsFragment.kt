package com.words.android.ui.details

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.words.android.MainViewModel
import com.words.android.R
import com.words.android.databinding.DetailsFragmentBinding
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.users.UserWordType
import com.words.android.data.repository.WordSource
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
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


    private val adapter: DetailsAdapter = DetailsAdapter(this)

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

        sharedViewModel.currentSources.observe(this, Observer { source ->
            sharedViewModel.setCurrentWordRecented()
            when (source) {
                is WordSource.FirestoreUserSource -> {
                    setUserWord(source.userWord)
                }
            }
            adapter.submitWordSource(source)
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
        Snackbar.make(detailsRoot, message, Snackbar.LENGTH_SHORT)
                .configError(context!!, true)
                .show()
    }

    override fun onMerriamWebsterDismissClicked() {
        //TODO set user setting
        adapter.removeWordSource(WordSource.MerriamWebsterSource::class)
    }

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null)  return

        val favoriteMenuItem = toolbar.menu?.findItem(R.id.action_favorite)
        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)
        favoriteMenuItem?.isChecked = isFavorited
        favoriteMenuItem?.icon = ContextCompat.getDrawable(context!!, if (isFavorited) R.drawable.ic_round_favorite_black_24px else R.drawable.ic_round_favorite_border_black_24px)

    }

}

