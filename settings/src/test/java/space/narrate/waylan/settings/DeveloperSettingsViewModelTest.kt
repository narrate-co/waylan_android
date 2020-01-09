package space.narrate.waylan.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.settings.ui.developer.DeveloperSettingsViewModel
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.valueBlocking
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class DeveloperSettingsViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var developerSettingsViewModel: DeveloperSettingsViewModel
    private val userRepository = mock(UserRepository::class.java)

    private val user: MutableLiveData<User> = MutableLiveData()

    @Before
    fun setUp() {
        developerSettingsViewModel = DeveloperSettingsViewModel(userRepository)
    }

    @Test
    fun useTestSkusChanged_shouldUpdateUseTestSkus() {
        val data: MutableLiveData<Boolean> = MutableLiveData()
        whenever(userRepository.useTestSkusLive).thenReturn(data)

        data.value = false

        assertThat(developerSettingsViewModel.useTestSkus.valueBlocking).isFalse()

        data.value = true

        assertThat(developerSettingsViewModel.useTestSkus.valueBlocking).isTrue()
    }

}