package space.narrate.waylan.android.ui.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode
import space.narrate.waylan.android.R
import space.narrate.waylan.core.ui.ListType
import space.narrate.waylan.core.ui.widget.ProgressUnderlineView
import space.narrate.waylan.test_common.RecyclerViewMatcher
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
class HomeFragmentTest: AutoCloseKoinTest() {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val homeViewModel = mock(HomeViewModel::class.java)

    private val trendingTitle = context.getString(R.string.title_trending)
    private val recentTitle = context.getString(R.string.title_recent)
    private val favoriteTitle = context.getString(R.string.title_favorite)
    private val settingsTitle = context.getString(R.string.title_settings)

    private val trendingPreview = "epithet, impetuous, fecund, name"
    private val recentPreview = "pious, portend, ennui"
    private val favoritePreview = "fecund, epithet, name"

    private var listData: List<HomeItemModel> = listOf(
        HomeItemModel.ItemModel(ListType.TRENDING, R.string.title_trending, trendingPreview),
        HomeItemModel.ItemModel(ListType.RECENT, R.string.title_recent, recentPreview),
        HomeItemModel.ItemModel(ListType.FAVORITE, R.string.title_favorite, favoritePreview),
        HomeItemModel.DividerModel,
        HomeItemModel.SettingsModel
    )
    private val listLiveData: MutableLiveData<List<HomeItemModel>> = MutableLiveData()

    @Before
    fun setUp() {
        listLiveData.value = listData
        whenever(homeViewModel.list).thenReturn(listLiveData)

        loadKoinModules(
            module(override = true) {
                viewModel { homeViewModel }
            }
        )
    }

    @Test
    fun shouldShowDestinationsList() {
        launchFragment()

        // Check things are in the correct order
        onView(RecyclerViewMatcher(R.id.recycler_view)
            .atPositionOnView(0, R.id.title_text_view))
            .check(matches(withText(trendingTitle)))

        onView(RecyclerViewMatcher(R.id.recycler_view)
            .atPositionOnView(1, R.id.title_text_view))
            .check(matches(withText(recentTitle)))

        onView(RecyclerViewMatcher(R.id.recycler_view)
            .atPositionOnView(2, R.id.title_text_view))
            .check(matches(withText(favoriteTitle)))

        onView(RecyclerViewMatcher(R.id.recycler_view)
            .atPositionOnView(3))
            .check(matches(instanceOf(ProgressUnderlineView::class.java)))

        onView(RecyclerViewMatcher(R.id.recycler_view)
            .atPositionOnView(4, R.id.title_text_view))
            .check(matches(withText(settingsTitle)))

        // Knowing everything is in the correct order, simply check all additional
        // information is shown.
        onView(withText(trendingPreview)).check(matches(isDisplayed()))
        onView(withText(recentPreview)).check(matches(isDisplayed()))
        onView(withText(favoritePreview)).check(matches(isDisplayed()))
    }

    @Test
    fun onTrendingClicked_shouldNavigateToTrending() {
        onViewWithTextClick_shouldNavigateToListType(trendingTitle, ListType.TRENDING)
    }

    @Test
    fun onRecentClicked_shouldNavigateToRecent() {
        onViewWithTextClick_shouldNavigateToListType(recentTitle, ListType.RECENT)
    }

    @Test
    fun onFavoriteClicked_shouldNavigateToFavorite() {
        onViewWithTextClick_shouldNavigateToListType(favoriteTitle, ListType.FAVORITE)
    }

    @Test
    fun onSettingsClicked_shouldNavigateToSettings() {
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        onView(withText(settingsTitle)).perform(click())

        verify(navController).navigate(R.id.action_homeFragment_to_settingsFragment)
    }

    private fun onViewWithTextClick_shouldNavigateToListType(text: String, type: ListType) {
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        onView(withText(text)).perform(click())

        verify(navController).navigate(
            HomeFragmentDirections.actionHomeFragmentToListFragment(type)
        )
    }

    private fun launchFragment(navController: NavController? = null) {
        val scenario = launchFragmentInContainer<HomeFragment>(
            themeResId = R.style.Theme_Waylan_DayNight
        )
        scenario.onFragment {
            if (navController != null) {
                Navigation.setViewNavController(it.requireView(), navController)
            }
        }
    }
}
