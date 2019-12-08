package space.narrate.waylan.core.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import space.narrate.waylan.core.ui.widget.CheckPreferenceView

/**
 * TODO: Test not working.
 */
@RunWith(RobolectricTestRunner::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
class CheckPreferenceViewTest {

    private val testTitle = "Test title"
    private val testDesc = "This is a test desc"

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var checkPreferenceView: CheckPreferenceView

    @Before
    fun setUp() {
        checkPreferenceView = CheckPreferenceView(context)
    }

    @Test
    fun setTitle_shouldShowOnlyTitle() {
        checkPreferenceView.setTitle(testTitle)

        onView(withText(testTitle)).check(matches(isDisplayed()))
    }
}