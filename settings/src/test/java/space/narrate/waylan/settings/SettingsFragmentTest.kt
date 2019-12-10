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
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.repo.FirestoreTestData
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.settings.ui.settings.MwBannerModel
import space.narrate.waylan.settings.ui.settings.NightModeRadioItemModel
import space.narrate.waylan.settings.ui.settings.OrientationRadioItemModel
import space.narrate.waylan.settings.ui.settings.PurchaseFlowModel
import space.narrate.waylan.settings.ui.settings.SettingsFragment
import space.narrate.waylan.settings.ui.settings.SettingsViewModel
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
class SettingsFragmentTest: AutoCloseKoinTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val settingsViewModel = mock(SettingsViewModel::class.java)
    private val navigator = mock(Navigator::class.java)

    private val nightMode: MutableLiveData<NightMode> = MutableLiveData()
    private val orientation: MutableLiveData<Orientation> = MutableLiveData()
    private val bannerModel: MutableLiveData<MwBannerModel> = MutableLiveData()
    private val shouldLaunchSignUp: MutableLiveData<Event<Boolean>> = MutableLiveData()
    private val shouldLaunchLogIn: MutableLiveData<Event<Boolean>> = MutableLiveData()
    private val shouldLaunchMwPurchaseFlow: MutableLiveData<Event<PurchaseFlowModel>> = MutableLiveData()
    private val shouldShowNightModeDialog: MutableLiveData<Event<List<NightModeRadioItemModel>>> = MutableLiveData()
    private val shouldShowOrientationDialog: MutableLiveData<Event<List<OrientationRadioItemModel>>> = MutableLiveData()


    @Before
    fun setUp() {
        whenever(settingsViewModel.nightMode).thenReturn(nightMode)
        whenever(settingsViewModel.orientation).thenReturn(orientation)
        whenever(settingsViewModel.bannerModel).thenReturn(bannerModel)
        whenever(settingsViewModel.shouldLaunchLogIn).thenReturn(shouldLaunchLogIn)
        whenever(settingsViewModel.shouldLaunchSignUp).thenReturn(shouldLaunchSignUp)
        whenever(settingsViewModel.shouldLaunchMwPurchaseFlow).thenReturn(shouldLaunchMwPurchaseFlow)
        whenever(settingsViewModel.shouldShowNightModeDialog).thenReturn(shouldShowNightModeDialog)
        whenever(settingsViewModel.shouldShowOrientationDialog).thenReturn(shouldShowOrientationDialog)

        loadKoinModules(
            module(override = true) {
                single { navigator }
                viewModel { settingsViewModel }
            }
        )
    }

    @Test
    fun registeredFreeUser_shouldShowLaunchMwPurchaseFlowButton() {
        bannerModel.value = MwBannerModel.create(FirestoreTestData.registeredFreeValidUser)

        // Act
        launchFragment()

        // Assert
        val addButtonText = context.getString(R.string.settings_header_registered_add_button)
        onView(withText(addButtonText)).check(matches(isDisplayed()))
    }

    @Test
    fun registeredFreeUserOnPurchase_shouldUpdateBanner() {
        bannerModel.value = MwBannerModel.create(FirestoreTestData.registeredFreeValidUser)

        launchFragment()

        val addBody = context.getString(R.string.settings_header_registered_free_trial_body)
        val addButton = context.getString(R.string.settings_header_registered_add_button)
        val addedLabel = context.getString(R.string.settings_header_added_label)
        val addedBody = context.getString(R.string.settings_header_registered_subscribed_body)
        onView(withText(addButton)).check(matches(isDisplayed()))
        onView(withText(addBody)).check(matches(isDisplayed()))

        bannerModel.value = MwBannerModel.create(FirestoreTestData.registeredPurchasedValidUser)

        onView(withText(addedBody)).check(matches(isDisplayed()))
        onView(withText(addedLabel)).check(matches(isDisplayed()))
    }

    private fun launchFragment(navController: NavController? = null) {
        val scenario = launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_Waylan_DayNight
        )
        scenario.onFragment {
            if (navController != null) {
                Navigation.setViewNavController(it.requireView(), navController)
            }
        }
    }
}
