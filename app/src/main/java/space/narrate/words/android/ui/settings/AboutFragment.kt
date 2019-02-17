package space.narrate.words.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about.view.*
import space.narrate.words.android.BuildConfig
import space.narrate.words.android.Navigator
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment


/**
 * A Fragment to display a short about copy as well as miscellaneous items about the Words
 * application and software
 *
 * [R.id.aboutBody] A copy explaining what Words is as a product/company
 * [R.id.version] The build's version name and type
 * [R.id.thirdPartyLibraries] Leads to [ThirdPartyLibrariesFragment]
 */
class AboutFragment: BaseUserFragment() {

    companion object {
        // A tag used for back stack tracking
        const val FRAGMENT_TAG = "about_fragment_tag"

        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.onBackPressed()
        }

        // Version preference
        view.version.setDesc("v${BuildConfig.VERSION_NAME} • ${BuildConfig.BUILD_TYPE}")

        // Third Party Libs preference
        view.thirdPartyLibraries.setOnClickListener {
            Navigator.showThirdPartyLibraries(activity!!)
        }

        return view
    }
}