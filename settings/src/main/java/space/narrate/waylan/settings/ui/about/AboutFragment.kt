package space.narrate.waylan.settings.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.android.ext.android.inject
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.widget.ElasticTransition
import space.narrate.waylan.settings.BuildConfig
import space.narrate.waylan.settings.R
import space.narrate.waylan.settings.databinding.FragmentAboutBinding

/**
 * A Fragment to display a short about copy as well as miscellaneous items about the Words
 * application and software
 *
 * [R.id.aboutBody] A copy explaining what Words is as a product/company
 * [R.id.version_preference] The build's version name and listType
 * [R.id.third_party_libs_preference] Leads to [ThirdPartyLibrariesFragment]
 */
class AboutFragment: Fragment() {

    private lateinit var binding: FragmentAboutBinding

    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
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

        binding.run {

            appBar.doOnElasticDrag(
                alphaViews = listOf(scrollView, appBar)
            )

            appBar.doOnElasticDismiss {
                navigator.toBack(Navigator.BackType.DRAG, this.javaClass.simpleName)
            }

            appBar.setOnNavigationIconClicked {
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }

            appBar.setReachableContinuityNavigator(this@AboutFragment, navigator)

            // Version preference
            versionPreference.setDesc("xyz • " +
                    "0.0.1 • ${BuildConfig.BUILD_TYPE}")

//            versionPreference.setDesc("${BuildConfig.VERSION_NAME} • " +
//                "${BuildConfig.VERSION_CODE} • ${BuildConfig.BUILD_TYPE}")

            // Third Party Libs preference
            thirdPartyLibsPreference.setOnClickListener {
                findNavController().navigate(
                    R.id.action_aboutFragment_to_thirdPartyLibrariesFragment
                )
            }
        }

        startPostponedEnterTransition()
    }
}