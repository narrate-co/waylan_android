package space.narrate.waylan.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.util.toLiveData
import space.narrate.waylan.settings.ui.addons.AddOnItemModel
import space.narrate.waylan.settings.ui.addons.AddOnsViewModel
import space.narrate.waylan.test_common.FirestoreTestData
import space.narrate.waylan.test_common.anyOrNull
import org.mockito.Mockito.`when` as whenever

class AddOnsViewModelTest {

    private val userRepository = mock(UserRepository::class.java)
    private val analyticsRepository = mock(AnalyticsRepository::class.java)

    private lateinit var addOnsViewModel: AddOnsViewModel

    private val testUser = FirestoreTestData.testDatabase.users[1]

    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        whenever(userRepository.getUserAddOnLive(anyOrNull()))
            .thenAnswer { invocation: InvocationOnMock? ->
                val addOn = invocation?.getArgument<AddOn>(0)
                val data = testUser.addOns.firstOrNull { it.id == addOn?.id }
                data.toLiveData
            }
        whenever(userRepository.user).thenReturn(testUser.user.toLiveData)
        addOnsViewModel = AddOnsViewModel(userRepository, analyticsRepository)
    }

    @Test
    fun onActionClicked_shouldUpdateUser() {

        addOnsViewModel.onActionClicked(
            AddOnItemModel.MerriamWebster(AddOn.MERRIAM_WEBSTER, testUser.addOns[0]),
            AddOnAction.TRY_FOR_FREE
        )

        verify(userRepository)
            .updateUserAddOn(AddOn.MERRIAM_WEBSTER, UserAddOnActionUseCase.TryForFree)
    }
}