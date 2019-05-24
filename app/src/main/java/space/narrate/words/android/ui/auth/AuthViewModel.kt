package space.narrate.words.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import space.narrate.words.android.data.Result
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.auth.FirebaseAuthWordsException
import space.narrate.words.android.ui.common.Event
import kotlin.Exception
import kotlin.coroutines.CoroutineContext

class AuthViewModel(
    private val authenticationStore: AuthenticationStore
): ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val _authRoute: MutableLiveData<AuthRoute> = MutableLiveData()
    val authRoute: LiveData<AuthRoute>
        get() = _authRoute

    private val _shouldShowCredentials: MutableLiveData<Event<ShowCredentialsModel>> =
        MutableLiveData()
    val shouldShowCredentials: LiveData<Event<ShowCredentialsModel>>
        get() = _shouldShowCredentials

    private val _shouldLaunchMain: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldLaunchMain: LiveData<Event<Boolean>>
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
                if (authenticationStore.hasFirestoreUser) {
                    authenticate()
                } else {
                    signUpAnonymously()
                }
            }
        }
    }

    fun onAnyTextChanged() {
        _shouldShowError.postValue(Event(ShowErrorModel.None))
    }

    fun onCancelClicked() {
        if (authenticationStore.hasUser) {
            authenticate()
        }
    }

    fun onLoginClicked(email: String, password: String) {
        launch {
            _showLoading.postValue(true)
            val result = authenticationStore.logIn(email, password)
            when (result) {
                is Result.Success -> postLaunchMain()
                is Result.Error -> postError(result.exception)
            }
            _showLoading.postValue(false)
        }
    }

    fun onSignUpClicked(email: String, password: String, confirmPassword: String) {
        launch {
            _showLoading.postValue(true)
            val result = authenticationStore.signUp(
                email,
                password,
                confirmPassword
            )
            when (result) {
                is Result.Success -> postLaunchMain()
                is Result.Error -> postError(result.exception)
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

    private fun signUpAnonymously() {
        launch {
            _showLoading.postValue(true)
            val result = authenticationStore.signUpAnonymously()
            when (result) {
                is Result.Success -> postLaunchMain()
                is Result.Error -> postError(result.exception)
            }
            _showLoading.postValue(false)
        }
    }

    private fun authenticate() {
        launch {
            _showLoading.postValue(true)
            val result = authenticationStore.authenticate()
            when (result) {
                is Result.Success -> postLaunchMain()
                is Result.Error -> postError(result.exception)
            }
            _showLoading.postValue(false)
        }
    }

    private fun postLaunchMain() {
        _shouldLaunchMain.postValue(Event(true))
    }

    private fun postError(e: Exception) {
        val error = when (e) {
            is FirebaseAuthWordsException -> ShowErrorModel.Error(e.localizedMessageRes)
            else -> ShowErrorModel.Error(null, e.localizedMessage)
        }
        _shouldShowError.postValue(Event(error))
    }




}

