package com.words.android.ui.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.words.android.*
import kotlinx.android.synthetic.main.list_fragment.view.*

class ListFragment: WFragment(), ListTypeAdapter.ListTypeListener {


    enum class ListType(val fragmentTag: String, val title: String) {
        TRENDING("trending_fragment_tag", "Trending"), RECENT("recent_fragment_tag", "Recent"), FAVORITE("favorite_fragment_tag", "Favorite")
    }

    companion object {
        fun newTrendingInstance(): ListFragment = newInstance(ListType.TRENDING)
        fun newRecentInstance(): ListFragment = newInstance(ListType.RECENT)
        fun newFavoriteInstance(): ListFragment = newInstance(ListType.FAVORITE)

        fun newInstance(type: ListType): ListFragment {
            val listFrag = ListFragment()
            val args = Bundle()
            args.putString("type", type.name)
            listFrag.arguments = args
            return listFrag
        }
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(activity!!, (activity!!.application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    var type: ListType = ListType.TRENDING

    private val adapter by lazy { ListTypeAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_fragment, container, false)
        type = when (arguments?.getString("type")) {
            ListType.TRENDING.name -> ListType.TRENDING
            ListType.RECENT.name -> ListType.RECENT
            ListType.FAVORITE.name -> ListType.FAVORITE
            else -> ListType.TRENDING
        }
        view.toolbarTitle.text = type.title
        view.navigationIcon.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        setUpList(view)

        return view
    }


    private fun setUpList(view: View) {
        view.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerView.adapter = adapter
        val itemDivider = ListItemDivider(ContextCompat.getDrawable(context!!, R.drawable.list_item_divider))
        view.recyclerView.addItemDecoration(itemDivider)

        sharedViewModel.getList(type).observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun onWordClicked(word: String) {
        sharedViewModel.setCurrentWordId(word)
        (activity as? MainActivity)?.showDetails()
    }


}