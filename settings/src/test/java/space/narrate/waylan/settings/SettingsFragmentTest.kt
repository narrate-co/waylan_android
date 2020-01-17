package space.narrate.waylan.settings

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.ui.widget.ReachabilityAppBarLayout
import space.narrate.waylan.settings.ui.settings.*
import space.narrate.waylan.test_common.TestApp
import org.mockito.Mockito.`when` as whenever

//@RunWith(AndroidJUnit4::class)
//@Config(application = TestApp::class)
//@MediumTest
//@LooperMode(LooperMode.Mode.PAUSED)
//@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
//class SettingsFragmentTest: AutoCloseKoinTest() {
//
//    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private val context: Context = ApplicationProvider.getApplicationContext()
//    private val settingsViewModel = mock(SettingsViewModel::class.java)
//    private val navigator = mock(Navigator::class.java)
//
//    private val nightMode: MutableLiveData<NightMode> = MutableLiveData()
//    private val orientation: MutableLiveData<Orientation> = MutableLiveData()
//    private val shouldLaunchSignUp: MutableLiveData<Event<Boolean>> = MutableLiveData()
//    private val shouldLaunchLogIn: MutableLiveData<Event<Boolean>> = MutableLiveData()
//    private val shouldShowNightModeDialog: MutableLiveData<Event<List<NightModeRadioItemModel>>> = MutableLiveData()
//    private val shouldShowOrientationDialog: MutableLiveData<Event<List<OrientationRadioItemModel>>> = MutableLiveData()
//    private val logInSignOutModel = MutableLiveData<LogInSignOutModel>()
//    private val navigatorReachabilityState = MutableLiveData<ReachabilityAppBarLayout.ReachableContinuityNavigator.State>()
//
//    @Before
//    fun setUp() {
//        whenever(settingsViewModel.nightMode).thenReturn(nightMode)
//        whenever(settingsViewModel.orientation).thenReturn(orientation)
//        whenever(settingsViewModel.shouldLaunchLogIn).thenReturn(shouldLaunchLogIn)
//        whenever(settingsViewModel.shouldLaunchSignUp).thenReturn(shouldLaunchSignUp)
//        whenever(settingsViewModel.shouldShowNightModeDialog).thenReturn(shouldShowNightModeDialog)
//        whenever(settingsViewModel.shouldShowOrientationDialog).thenReturn(shouldShowOrientationDialog)
//        whenever(settingsViewModel.logInSignOut).thenReturn(logInSignOutModel)
//        whenever(navigator.reachabilityState).thenReturn(navigatorReachabilityState)
//
//        loadKoinModules(
//                module(override = true) {
//                    single { navigator }
//                    single { settingsViewModel }
//                }
//        )
//    }
//
//    @Test
//    fun shouldShowAddOnsPreferenceTitle() {
//        launchFragment()
//
//        val settingsTitle = context.getString(R.string.add_ons_title)
//        onView(withText(settingsTitle)).check(matches(isDisplayed()))
//    }
//
//    private fun launchFragment(navController: NavController? = null) {
//        val scenario = launchFragmentInContainer<SettingsFragment>(
//            themeResId = R.style.Theme_Waylan_DayNight
//        )
//        scenario.onFragment {
//            if (navController != null) {
//                Navigation.setViewNavController(it.requireView(), navController)
//            }
//        }
//    }
//}
