package space.narrate.words.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.narrate.words.android.data.auth.Auth
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.auth.FirebaseAuthWordsException
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.ui.Event
import javax.inject.Inject
import kotlin.Exception
import kotlin.coroutines.CoroutineContext

/**
 * A ViewModel that handles the manipulation of [FirebaseUser] and [User] to create
 * valid [Auth] objects.
 */
class AuthViewModel @Inject constructor(): ViewModel(), CoroutineScope {

    // TODO Move into constructor and inject
    private val authenticationStore: AuthenticationStore = AuthenticationStore()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val _authRoute: MutableLiveData<AuthRoute> = MutableLiveData()
    val authRoute: LiveData<AuthRoute>
        get() = _authRoute

    private val _shouldShowCredentials: MutableLiveData<Event<ShowCredentialsModel>> =
        MutableLiveData()
    val shouldShowCredentials: LiveData<Event<ShowCredentialsModel>>
        get() = _shouldShowCredentials

    private val _shouldLaunchMain: MutableLiveData<Event<LaunchMainModel>> = MutableLiveData()
    val shouldLaunchMain: LiveData<Event<LaunchMainModel>>
        get() = _shouldLaunchMain

    private val _shouldShowError: MutableLiveData<Event<ShowErrorModel>> = MutableLiveData()
    val shouldShowError: LiveData<Event<ShowErrorModel>>
        get() = _shouldShowError

    private val _showLoading: MutableLiveData<Boolean> = MutableLiveData()
    val showLoading: LiveData<Boolean>
        get() = _showLoading


    fun onAuthRouteReceived(authRoute: AuthRoute) {
        _authRoute.postValue(authRoute)

        when (authRoute) {
            AuthRoute.SIGN_UP,
            AuthRoute.LOG_IN -> _shouldShowCredentials.postValue(Event(ShowCredentialsModel()))
            else -> {
                if (authenticationStore.hasFirebaseUser) {
                    getCurrentAuthAndReturnToMain()
                } else {
                    signUpAnonymouslyAndReturnToMain()
                }
            }
        }
    }

    fun onAnyTextChanged() {
        _shouldShowError.postValue(Event(ShowErrorModel.None))
    }

    fun onCancelClicked() {
        if (authenticationStore.hasFirebaseUser) {
            getCurrentAuthAndReturnToMain()
        }
    }

    fun onLoginClicked(email: String, password: String) {
        launch {
            _showLoading.postValue(true)
            try {
                val auth = authenticationStore.logIn(email, password)
                postLaunchMain(auth)
            } catch (e: Exception) {
                postError(e)
            }
            _showLoading.postValue(false)
        }
    }

    fun onSignUpClicked(email: String, password: String, confirmPassword: String) {
        launch {
            _showLoading.postValue(true)
            try {
                val auth = authenticationStore.signUp(
                    email,
                    password,
                    confirmPassword
                )
                postLaunchMain(auth)
            } catch (e: Exception) {
                postError(e)
            }
            _showLoading.postValue(false)
        }
    }

    fun onSignUpAlternateClicked() {
        _authRoute.postValue(AuthRoute.SIGN_UP)
    }

    fun onLoginAlternateClicked() {
        _authRoute.postValue(AuthRoute.LOG_IN)
    }

    private fun signUpAnonymouslyAndReturnToMain() {
        launch {
            _showLoading.postValue(true)
            try {
                val auth = authenticationStore.signUpAnonymously()
                postLaunchMain(auth)
            } catch (e: Exception) {
                postError(e)
            }
            _showLoading.postValue(false)
        }
    }

    private fun getCurrentAuthAndReturnToMain() {
        launch {
            _showLoading.postValue(true)
            try {
                val auth = authenticationStore.getCurrentAuth()
                postLaunchMain(auth)
            } catch (e: Exception) {
                postError(e)
            }
            _showLoading.postValue(false)
        }
    }

    private fun postLaunchMain(auth: Auth?, clearStack: Boolean = true) {
        _shouldLaunchMain.postValue(Event(LaunchMainModel(auth, clearStack)))
    }

    private fun postError(e: Exception) {
        val error = when (e) {
            is FirebaseAuthWordsException -> ShowErrorModel.Error(e.localizedMessageRes)
            else -> ShowErrorModel.Error(null, e.localizedMessage)
        }
        _shouldShowError.postValue(Event(error))
    }




}

