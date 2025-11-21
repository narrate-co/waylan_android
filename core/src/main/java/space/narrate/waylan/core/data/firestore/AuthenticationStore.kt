package space.narrate.waylan.core.data.firestore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.Result.Success
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.prefs.PreferenceStore
import kotlin.coroutines.CoroutineContext

/**
 * A store which knows about the credentials which back a [User] - the global account object.
 *
 * This class can [authenticate] a [User] or help create a new [User] by using [logIn], [signUp]
 * or [signUpAnonymously].
 */
class AuthenticationStore(
    private val preferenceStore: PreferenceStore
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    /**
     * Whether or not the credentials necessary to authenticate a [User] are available.
     *
     * If they are, the client can call [authenticate] and refresh the [user] LiveData. Otherwise,
     * the client must use [logIn], [signUp] or [signUpAnonymously] to get/create the necessary
     * credentials and refresh [user].
     */
    val hasCredentials: Boolean
        get() = preferenceStore.uid.getValue().isNotEmpty()

    val hasUser: Boolean
        get() = _uid.value != null && _uid.value?.isNotEmpty() == true

    private val _uid: MutableLiveData<String> = MutableLiveData()
    val uid: LiveData<String>
        get() = _uid

    init {
        // If the user's uid is stored in preferences, retrieve it so auth can be skipped and done
        // in the background, avoiding the splash screen when recreating the app.
        _uid.value = preferenceStore.uid.getValue()
    }

    suspend fun authenticate(): Result<User> {
        val result = getOrCreateUser()

        if (result is Success) {
            // Launch and forget an update. This shouldn't block the authentication process
            _uid.value = result.data.uid
            // Save the uid to preferences for faster startup times.
            preferenceStore.uid.setValue(result.data.uid)
        }

        return result
    }

    suspend fun logIn(email: String, password: String): Result<User> {
        return authenticate()
    }

    suspend fun signUp(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        return authenticate()
    }

    suspend fun signUpAnonymously(): Result<User> {
        return authenticate()
    }


    private suspend fun getOrCreateUser(): Result<User> {
        if (preferenceStore.uid.getValue().isEmpty()) {
            preferenceStore.uid.setValue("123dev")
        }
        return Success(User(
            preferenceStore.uid.getValue(),
            true,
            "Tester",
            "tester@hey.com",
        ))
    }
}