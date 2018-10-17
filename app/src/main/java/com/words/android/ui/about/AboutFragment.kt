package com.words.android.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.words.android.R
import com.words.android.ui.common.BaseUserFragment
import kotlinx.android.synthetic.main.list_fragment.view.*


class AboutFragment: BaseUserFragment() {

    companion object {
        const val FRAGMENT_TAG = "about_fragment_tag"
        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.about_fragment, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        return view
    }

}