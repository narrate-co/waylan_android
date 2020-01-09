package space.narrate.waylan.settings

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import space.narrate.waylan.core.data.firestore.FirestoreTestData
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.settings.ui.addons.AddOnItemModel
import space.narrate.waylan.settings.ui.addons.AddOnsViewModel

class AddOnsViewModelTest {

    private val userRepository = mock(UserRepository::class.java)
    private val analyticsRepository = mock(AnalyticsRepository::class.java)

    private lateinit var addOnsViewModel: AddOnsViewModel

    private val testUser = FirestoreTestData.testDatabase.users[1]

    @Before
    fun setUp() {
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