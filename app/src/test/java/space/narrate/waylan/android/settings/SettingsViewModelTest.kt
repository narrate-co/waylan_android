package space.narrate.waylan.android.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import space.narrate.waylan.core.data.repo.FirestoreTestData
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.repo.UserRepository
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.valueBlocking
import java.util.*
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var settingsViewModel: SettingsViewModel
    private val userRepository = mock(UserRepository::class.java)

    private val user = MutableLiveData<User>()

    @Before
    fun setUp() {
        settingsViewModel = SettingsViewModel(userRepository)
    }

    @Test
    fun nonAnonymousUserOnPluginStateChanged_shouldUpdateBanner() {
        val registeredFreeUser = FirestoreTestData.registeredFreeValidUser
        user.value = registeredFreeUser
        whenever(userRepository.user).thenReturn(user)

        val bannerObserver = settingsViewModel.bannerModel

        assertThat(bannerObserver.valueBlocking.topButtonAction).isEqualTo(
            MwBannerAction.LAUNCH_PURCHASE_FLOW
        )

        user.value = registeredFreeUser.copy(
            merriamWebsterStarted = Date(),
            merriamWebsterPurchaseToken = "aslkfjwoeir23nasd"
        )

        val banner = bannerObserver.valueBlocking
        assertThat(banner.topButtonAction).isNull()
        assertThat(banner.labelRes).isEqualTo(R.string.settings_header_added_label)
        assertThat(banner.textRes).isEqualTo(R.string.settings_header_registered_subscribed_body)
    }
}