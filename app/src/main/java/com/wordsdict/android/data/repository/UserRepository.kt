package com.wordsdict.android.data.repository

import androidx.lifecycle.LiveData
import com.wordsdict.android.data.firestore.FirestoreStore
import com.wordsdict.android.data.firestore.users.PluginState
import com.wordsdict.android.data.firestore.users.User
import com.wordsdict.android.data.firestore.users.merriamWebsterState
import com.wordsdict.android.data.prefs.UserPreferenceRepository
import com.wordsdict.android.util.LiveDataHelper
import kotlinx.coroutines.launch

class UserRepository(
        private val firestoreStore: FirestoreStore?,
        private val userPreferenceRepository: UserPreferenceRepository
) {

    fun getUser(): LiveData<User> =
        firestoreStore?.getUserLive() ?: LiveDataHelper.empty()

    fun setUserMerriamWebsterState(state: PluginState) {
        println("UserRepository::setUserMerriamWebsterState = $state")
        launch {
            firestoreStore?.setUserMerriamWebsterState(state)
        }
    }
}