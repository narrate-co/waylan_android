package space.narrate.words.android.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.BuildConfig
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.setUpWithElasticBehavior
import space.narrate.words.android.util.widget.CheckPreferenceView
import space.narrate.words.android.util.widget.ElasticTransition


/**
 * A Fragment to display a short about copy as well as miscellaneous items about the Words
 * application and software
 *
 * [R.id.aboutBody] A copy explaining what Words is as a product/company
 * [R.id.version_preference] The build's version name and listType
 * [R.id.third_party_libs_preference] Leads to [ThirdPartyLibrariesFragment]
 */
class AboutFragment: BaseUserFragment() {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var navigationIcon: AppCompatImageButton
    private lateinit var versionPreference: CheckPreferenceView
    private lateinit var thirdPartyLibrariesPreference: CheckPreferenceView

    private val sharedViewModel by lazy {
        ViewModelProviders
            .of(requireActivity(), viewModelFactory)
            .get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coordinatorLayout = view.findViewById(R.id.coordinator_layout)
        appBarLayout = view.findViewById(R.id.app_bar)
        scrollView = view.findViewById(R.id.scroll_view)
        navigationIcon = view.findViewById(R.id.navigation_icon)
        versionPreference = view.findViewById(R.id.version_preference)
        thirdPartyLibrariesPreference = view.findViewById(R.id.third_party_libs_preference)

        postponeEnterTransition()

        appBarLayout.setUpWithElasticBehavior(
            this.javaClass.simpleName,
            sharedViewModel,
            listOf(navigationIcon),
            listOf(scrollView, appBarLayout)
        )

        navigationIcon.setOnClickListener {
            sharedViewModel.onNavigationIconClicked(this.javaClass.simpleName)
        }

        // Version preference
        versionPreference.setDesc("v${BuildConfig.VERSION_NAME} • ${BuildConfig.BUILD_TYPE}")

        // Third Party Libs preference
        thirdPartyLibrariesPreference.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_thirdPartyLibrariesFragment)
        }
        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        coordinatorLayout.updatePadding(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight
        )
        scrollView.updatePadding(bottom = insets.systemWindowInsetBottom)
        return super.handleApplyWindowInsets(insets)
    }
}