package space.narrate.waylan.android.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.auth.AuthenticationStore
import space.narrate.waylan.core.data.auth.FirebaseAuthWordsException
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.PreferenceStore
import space.narrate.waylan.core.data.repo.AnalyticsRepository
import space.narrate.waylan.core.ui.common.Event
import kotlin.Exception

class AuthViewModel(
    private val authenticationStore: AuthenticationStore,
    private val preferenceStore: PreferenceStore,
    private val analyticsRepository: AnalyticsRepository
): ViewModel() {

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

    val nightMode: LiveData<NightMode>
        get() = preferenceStore.nightMode.getLive()

    fun onAuthRouteReceived(authRoute: AuthRoute) {
        _authRoute.postValue(authRoute)

        when (authRoute) {
            AuthRoute.SIGN_UP,
            AuthRoute.LOG_IN -> _shouldShowCredentials.postValue(Event(ShowCredentialsModel()))
            else -> {
                if (authenticationStore.hasCredentials) {
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

    fun onSignUpAlternateClicked() {
        _authRoute.postValue(AuthRoute.SIGN_UP)
    }

    fun onLoginAlternateClicked() {
        _authRoute.postValue(AuthRoute.LOG_IN)
    }

    fun onLoginClicked(email: String, password: String) = viewModelScope.launch {
        _showLoading.postValue(true)
        val result = authenticationStore.logIn(email, password)
        when (result) {
            is Result.Success -> postLaunchMain()
            is Result.Error -> postError(result.exception)
        }
        _showLoading.postValue(false)
    }

    fun onSignUpClicked(
        email: String,
        password: String,
        confirmPassword: String
    ) = viewModelScope.launch {
        _showLoading.postValue(true)
        val result = authenticationStore.signUp(
            email,
            password,
            confirmPassword
        )
        when (result) {
            is Result.Success -> {
                analyticsRepository.logSignUpEvent()
                postLaunchMain()
            }
            is Result.Error -> postError(result.exception)
        }
        _showLoading.postValue(false)
    }

    private fun authenticate() = viewModelScope.launch {
        _showLoading.postValue(true)
        val result = authenticationStore.authenticate()
        when (result) {
            is Result.Success -> postLaunchMain()
            is Result.Error -> postError(result.exception)
        }
        _showLoading.postValue(false)
    }

    private fun signUpAnonymously() = viewModelScope.launch {
        _showLoading.postValue(true)
        val result = authenticationStore.signUpAnonymously()
        when (result) {
            is Result.Success -> postLaunchMain()
            is Result.Error -> postError(result.exception)
        }
        _showLoading.postValue(false)
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

