package space.narrate.waylan.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import org.robolectric.RobolectricTestRunner
import space.narrate.waylan.android.data.auth.AuthenticationStore
import space.narrate.waylan.android.data.firestore.users.User
import space.narrate.waylan.android.util.DefaultingMutableLiveData
import space.narrate.waylan.android.valueBlocking

@RunWith(RobolectricTestRunner::class)
class PreferenceTest : KoinTest {

    // Mock Android's getMainLooper()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val sharedPrefs = DefaultingMutableLiveData<SharedPreferences>(
        PreferenceManager.getDefaultSharedPreferences(context)
    )

    private lateinit var preference: Preference<String>

    @Before
    fun setUp() {
        preference = Preference(TEST_KEY, TEST_VALUE_DEFAULT, sharedPrefs)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun newInstance_shouldReturnDefaultValue() {
        assertThat(preference.getValue()).isEqualTo(TEST_VALUE_DEFAULT)
    }

    @Test
    fun newInstance_shouldReturnDefaultValueLive() {
        assertThat(preference.getLive().valueBlocking).isEqualTo(TEST_VALUE_DEFAULT)
    }

    @Test
    fun newInstance_changeSharedPreferenceShouldChangeValueLive() {
        // Set preference to user and set value
        setSharedPreferencesForUser(USER_UID_1)
        preference.setValue(TEST_VALUE_USER_1)

        // Get a live data object for the preference value
        val observer = preference.getLive()

        // Make sure the value reflects our above setting
        assertThat(observer.valueBlocking).isEqualTo(TEST_VALUE_USER_1)

        // Change the observers source back to default
        sharedPrefs.setDefault()

        // Assert that we are again given the default value
        assertThat(observer.valueBlocking).isEqualTo(TEST_VALUE_DEFAULT)
    }

    @Test
    fun liveValue_shouldUpdateWithSharedPreferenceChanges() {
        setSharedPreferencesForUser(USER_UID_1)
        preference.setValue(TEST_VALUE_USER_1)

        setSharedPreferencesForUser(USER_UID_2)
        preference.setValue(TEST_VALUE_USER_2)

        val observer = preference.getLive()

        setSharedPreferencesForUser(USER_UID_1)
        assertThat(observer.valueBlocking).isEqualTo(TEST_VALUE_USER_1)

        setSharedPreferencesForUser(USER_UID_2)
        assertThat(observer.valueBlocking).isEqualTo(TEST_VALUE_USER_2)
    }

    private fun setSharedPreferencesForUser(uid: String) {
        sharedPrefs.value = context.getSharedPreferences(
            uid,
            Context.MODE_PRIVATE
        )
    }

    companion object {
        private const val TEST_KEY = "test_key"
        private const val TEST_VALUE_DEFAULT = "123"
        private const val USER_UID_1 = "abc"
        private const val TEST_VALUE_USER_1 = "1234"
        private const val USER_UID_2 = "def"
        private const val TEST_VALUE_USER_2 = "12345"
    }

}