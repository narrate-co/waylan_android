package com.words.android.data.repository

import androidx.lifecycle.LiveData
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.util.LiveDataHelper
import kotlinx.coroutines.launch

class UserRepository(
        private val firestoreStore: FirestoreStore?,
        private val userPreferenceRepository: UserPreferenceRepository
) {

    fun getUser(): LiveData<User> =
        firestoreStore?.getUserLive() ?: LiveDataHelper.empty()

    fun setUserMerriamWebsterState(state: PluginState) {
        launch {
            firestoreStore?.setUserMerriamWebsterState(state)
        }
    }
}