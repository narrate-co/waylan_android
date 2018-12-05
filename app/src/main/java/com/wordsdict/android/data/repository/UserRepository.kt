package com.wordsdict.android.data.repository

import androidx.lifecycle.LiveData
import com.wordsdict.android.data.firestore.FirestoreStore
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.prefs.UserPreferenceStore
import com.wordsdict.android.util.LiveDataHelper
import kotlinx.coroutines.launch

/**
 * A repository for all data access to all Firestore [User] data.
 *
 * //TODO integrate userPreferenceStore and eliminate the use of [UserPreferenceStore]
 * //TODO in all ViewModel and "client" code.
 */
class UserRepository(
        private val firestoreStore: FirestoreStore?,
        private val userPreferenceStore: UserPreferenceStore
) {

    fun getUser(): LiveData<User> =
        firestoreStore?.getUserLive() ?: LiveDataHelper.empty()

    fun setUserMerriamWebsterState(state: PluginState) {
        launch {
            firestoreStore?.setUserMerriamWebsterState(state)
        }
    }
}