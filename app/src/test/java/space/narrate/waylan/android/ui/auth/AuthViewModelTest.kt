package space.narrate.waylan.android.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.auth.AuthenticationStore
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.prefs.PreferenceStore
import space.narrate.waylan.core.data.repo.AnalyticsRepository
import space.narrate.waylan.test_common.CoroutinesTestRule
import space.narrate.waylan.test_common.valueBlocking
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class AuthViewModelTest {

    // Subject under test.
    private lateinit var authViewModel: AuthViewModel

    private val authenticationStore = mock(AuthenticationStore::class.java)
    private val preferenceStore = mock(PreferenceStore::class.java)
    private val analyticsRepository = mock(AnalyticsRepository::class.java)

    private val TEST_USER = User("ABC")
    private val TEST_EXCEPTION = Exception("Error!")

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    // Mock Android's getMainLooper()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        authViewModel = AuthViewModel(authenticationStore, preferenceStore, analyticsRepository)
    }

    @Test
    fun onLoginRoute_shouldShowLogin() {
        authViewModel.onAuthRouteReceived(AuthRoute.LOG_IN)

        // The internal auth route should be set
        assertThat(authViewModel.authRoute.valueBlocking).isEqualTo(AuthRoute.LOG_IN)
        // The view should be told to show the credentials UI
        assertThat(authViewModel.shouldShowCredentials.valueBlocking.peek())
            .isEqualTo(ShowCredentialsModel())
    }

    @Test
    fun onSignUpRoute_shouldShowSignUp() {
        authViewModel.onAuthRouteReceived(AuthRoute.SIGN_UP)

        assertThat(authViewModel.authRoute.valueBlocking).isEqualTo(AuthRoute.SIGN_UP)
        assertThat(authViewModel.shouldShowCredentials.valueBlocking.peek())
            .isEqualTo(ShowCredentialsModel())
    }

    @Test
    fun onAnonymousRouteWithoutCredentials_shouldSuccessfullySignUpAnonymously() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            // We don't have any existing credentials
            whenever(authenticationStore.hasCredentials).thenReturn(false)
            // Successfully return a valid user from signUpAnonymously
            whenever(authenticationStore.signUpAnonymously()).thenReturn(Result.Success(TEST_USER))
            // We launch the anonymous flow
            authViewModel.onAuthRouteReceived(AuthRoute.ANONYMOUS)

            // The internal auth route should be set to anonymous
            assertThat(authViewModel.authRoute.valueBlocking)
                .isEqualTo(AuthRoute.ANONYMOUS)
            // The ViewModel should make a call to sign up anonymously
            verify(authenticationStore).signUpAnonymously()
            // Returning a valid user should cause MainActivity to be launched
            assertThat(authViewModel.shouldLaunchMain.valueBlocking.peek()).isTrue()
        }

    @Test
    fun onAnonymousRouteWithoutCredentials_shouldFailToSignUpAnonymously() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            whenever(authenticationStore.hasCredentials).thenReturn(false)
            whenever(authenticationStore.signUpAnonymously())
                .thenReturn(Result.Error(TEST_EXCEPTION))

            authViewModel.onAuthRouteReceived(AuthRoute.ANONYMOUS)

            assertThat(authViewModel.shouldShowError.valueBlocking.peek()).isEqualTo(
                ShowErrorModel.Error(null, TEST_EXCEPTION.localizedMessage)
            )
        }

    @Test
    fun onAnonymousRouteWithCredentials_shouldSuccessfullyAuthenticate() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            whenever(authenticationStore.hasCredentials).thenReturn(true)
            whenever(authenticationStore.authenticate()).thenReturn(Result.Success(TEST_USER))

            authViewModel.onAuthRouteReceived(AuthRoute.ANONYMOUS)

            assertThat(authViewModel.authRoute.valueBlocking)
                .isEqualTo(AuthRoute.ANONYMOUS)
            verify(authenticationStore).authenticate()
            assertThat(authViewModel.shouldLaunchMain.valueBlocking.peek()).isTrue()
        }

    @Test
    fun onAnonymousRouteWithCredentials_shouldFailToAuthenticate() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            whenever(authenticationStore.hasCredentials).thenReturn(true)
            whenever(authenticationStore.authenticate()).thenReturn(Result.Error(TEST_EXCEPTION))

            authViewModel.onAuthRouteReceived(AuthRoute.ANONYMOUS)

            assertThat(authViewModel.shouldShowError.valueBlocking.peek()).isEqualTo(
                ShowErrorModel.Error(null, TEST_EXCEPTION.localizedMessage)
            )
        }

    @Test
    fun onAlternateAuthenticationMethodClicked_shouldChangeAuthRoute() {
        // Configure the route with an initial value of log in
        authViewModel.onAuthRouteReceived(AuthRoute.LOG_IN)

        // Check that we're properly setting the route type
        assertThat(authViewModel.authRoute.valueBlocking).isEqualTo(AuthRoute.LOG_IN)

        // Act - Change the route to sign up
        authViewModel.onSignUpAlternateClicked()

        // Check that the auth route has changed to sign up
        assertThat(authViewModel.authRoute.valueBlocking).isEqualTo(AuthRoute.SIGN_UP)

        // Change the route back to log in
        authViewModel.onLoginAlternateClicked()

        // Check that the route is again log in
        assertThat(authViewModel.authRoute.valueBlocking).isEqualTo(AuthRoute.LOG_IN)
    }
}