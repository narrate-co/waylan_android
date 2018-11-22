package com.wordsdict.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wordsdict.android.BuildConfig
import com.wordsdict.android.Navigator
import com.wordsdict.android.R
import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.util.CheckPreferenceView
import kotlinx.android.synthetic.main.fragment_about.view.*


class AboutFragment: BaseUserFragment() {

    companion object {
        const val FRAGMENT_TAG = "about_fragment_tag"
        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }

        //version
        view.version.setDesc("v${BuildConfig.VERSION_NAME} • ${BuildConfig.BUILD_TYPE}")

        //third part libs
        view.thirdPartyLibraries.setOnClickListener {
            Navigator.showThirdPartyLibraries(activity!!)
        }

        return view
    }


}