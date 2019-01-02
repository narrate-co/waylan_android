package com.wordsdict.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserFragment

class ContextualFragment : BaseUserFragment() {

    companion object {
        fun newInstance() = ContextualFragment()
        const val TAG = "ContextualFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contextual, container, false)
    }
}