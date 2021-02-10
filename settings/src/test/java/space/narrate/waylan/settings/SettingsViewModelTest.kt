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
import space.narrate.waylan.settings.ui.settings.SettingsViewModel
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.valueBlocking
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
    fun nonAnonymousUser_singOutClickedShouldLaunchLogin() {
        val registeredUser = User("aaa", false, "Tester", "tester@test.com")
        user.value = registeredUser
        whenever(userRepository.user).thenReturn(user)

        settingsViewModel.onSignOutClicked()

        val event = settingsViewModel.shouldLaunchLogIn.valueBlocking
        assertThat(event.handled).isEqualTo(false)
        assertThat(event.peek()).isEqualTo(true)
    }

    // TODO: Add additional tests

}