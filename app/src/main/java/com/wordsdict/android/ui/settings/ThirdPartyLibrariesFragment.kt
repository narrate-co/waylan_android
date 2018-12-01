package com.wordsdict.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.ui.list.ListItemDivider
import kotlinx.android.synthetic.main.fragment_third_party_libraries.view.*
import com.wordsdict.android.Navigator
import com.wordsdict.android.util.emptyDiffItemCallback


class ThirdPartyLibrariesFragment: BaseUserFragment(), ThirdPartyLibraryViewHolder.ThirdPartyListener {


    companion object {
        const val FRAGMENT_TAG = "third_party_library_fragment_tag"
        fun newInstance() = ThirdPartyLibrariesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_third_party_libraries, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }

        return view
    }


    override fun onEnterTransactionEnded() {
        setUpList()
    }

    private fun setUpList() {
        view?.recycler?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val adapter = object : ListAdapter<ThirdPartyLibrary, ThirdPartyLibraryViewHolder>(emptyDiffItemCallback()) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThirdPartyLibraryViewHolder {
                return ThirdPartyLibraryViewHolder(
                        LayoutInflater
                                .from(parent.context!!)
                                .inflate(R.layout.settings_check_preference_list_item, parent, false),
                        this@ThirdPartyLibrariesFragment
                )
            }

            override fun onBindViewHolder(holder: ThirdPartyLibraryViewHolder, position: Int) {
                holder.bind(getItem(position))
            }
        }

        view?.recycler?.adapter = adapter
        val itemDivider = ListItemDivider(ContextCompat.getDrawable(context!!, R.drawable.list_item_divider))
        view?.recycler?.addItemDecoration(itemDivider)

        adapter.submitList(allThirdPartyLibraries)
    }

    override fun onClick(lib: ThirdPartyLibrary) {
        Navigator.launchWebsite(context!!, lib.url)
    }

}