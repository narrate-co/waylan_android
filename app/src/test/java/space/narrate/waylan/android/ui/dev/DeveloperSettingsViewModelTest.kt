package space.narrate.waylan.android.ui.dev

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import space.narrate.waylan.core.data.repo.FirestoreTestData
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.repo.UserRepository
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.capture
import space.narrate.waylan.test_common.valueBlocking
import java.util.*
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
    fun userChanges_shouldUpdateMwState() {
        whenever(userRepository.user).thenReturn(user)

        // Set the initial user to an invalid free user
        val testUser = FirestoreTestData.registeredFreeInvalidUser
        user.value = testUser

        val stateObserver = developerSettingsViewModel.mwState

        val state1 = stateObserver.valueBlocking
        assertThat(state1).isInstanceOf(PluginState.FreeTrial::class.java)
        assertThat(state1.isValid).isFalse()

        // Update the user to be a valid free trial user
        user.value = testUser.copy().apply { merriamWebsterStarted = Date() }

        val state2 = stateObserver.valueBlocking
        assertThat(state2).isInstanceOf(PluginState.FreeTrial::class.java)
        assertThat(state2.isValid).isTrue()
    }

    @Test
    fun registeredFreeValidUser_shouldUpdateToFreeExpired() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val testUser = FirestoreTestData.registeredFreeValidUser
            whenever(userRepository.getUser()).thenReturn(Result.Success(testUser))

            developerSettingsViewModel.onMwStatePreferenceClicked()

            val arg: ArgumentCaptor<PluginState> = ArgumentCaptor.forClass(PluginState::class.java)
            verify(userRepository).setUserMerriamWebsterState(capture(arg))

            assertThat(arg.value).isInstanceOf(PluginState.FreeTrial::class.java)
            assertThat(arg.value.isValid).isFalse()
        }

    @Test
    fun registeredFreeInvalidUser_shouldUpdateToPurchasedValid() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val testUser = FirestoreTestData.registeredFreeInvalidUser
            whenever(userRepository.getUser()).thenReturn(Result.Success(testUser))

            developerSettingsViewModel.onMwStatePreferenceClicked()

            val arg: ArgumentCaptor<PluginState> = ArgumentCaptor.forClass(PluginState::class.java)
            verify(userRepository).setUserMerriamWebsterState(capture(arg))

            assertThat(arg.value).isInstanceOf(PluginState.Purchased::class.java)
            assertThat(arg.value.purchaseToken).isNotEmpty()
            assertThat(arg.value.isValid).isTrue()
        }

    @Test
    fun registeredPurchasedValidUser_shouldUpdateToPurchasedInvalid() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val testUser = FirestoreTestData.registeredPurchasedValidUser
            whenever(userRepository.getUser()).thenReturn(Result.Success(testUser))

            developerSettingsViewModel.onMwStatePreferenceClicked()

            val arg: ArgumentCaptor<PluginState> = ArgumentCaptor.forClass(PluginState::class.java)
            verify(userRepository).setUserMerriamWebsterState(capture(arg))

            assertThat(arg.value).isInstanceOf(PluginState.Purchased::class.java)
            assertThat(arg.value.purchaseToken).isEqualTo(testUser.merriamWebsterPurchaseToken)
            assertThat(arg.value.isValid).isFalse()
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