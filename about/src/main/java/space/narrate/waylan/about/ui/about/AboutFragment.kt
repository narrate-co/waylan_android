package space.narrate.waylan.about.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import org.koin.android.viewmodel.ext.android.sharedViewModel
import space.narrate.waylan.about.R
import space.narrate.waylan.about.databinding.FragmentAboutBinding
import space.narrate.waylan.about.di.loadAboutModule
import space.narrate.waylan.android.BuildConfig
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.util.setUpWithElasticBehavior
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.android.R as waylanR

/**
 * A Fragment to display a short about copy as well as miscellaneous items about the Words
 * application and software
 *
 * [R.id.aboutBody] A copy explaining what Words is as a product/company
 * [R.id.version_preference] The build's version name and listType
 * [R.id.third_party_libs_preference] Leads to [ThirdPartyLibrariesFragment]
 */
class AboutFragment: BaseFragment() {

    private lateinit var binding: FragmentAboutBinding

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAboutModule()
        enterTransition = ElasticTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.run {
            appBar.setUpWithElasticBehavior(
                this.javaClass.simpleName,
                sharedViewModel,
                listOf(navigationIcon),
                listOf(scrollView, appBar)
            )

            navigationIcon.setOnClickListener {
                sharedViewModel.onNavigationIconClicked(this.javaClass.simpleName)
            }

            // Version preference
            versionPreference.setDesc("v${BuildConfig.VERSION_NAME} • ${BuildConfig.BUILD_TYPE}")

            // Third Party Libs preference
            thirdPartyLibsPreference.setOnClickListener {
                findNavController().navigate(
                    waylanR.id.action_aboutFragment_to_thirdPartyLibrariesFragment
                )
            }
        }

        startPostponedEnterTransition()
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        binding.run {
            coordinatorLayout.updatePadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight
            )
            scrollView.updatePadding(bottom = insets.systemWindowInsetBottom)
        }
        return super.handleApplyWindowInsets(insets)
    }
}