package com.words.android.ui.settings

import androidx.lifecycle.ViewModel
import com.words.android.data.firestore.users.User
import com.words.android.di.UserScope
import javax.inject.Inject

@UserScope
class SettingsViewModel @Inject constructor(private val user: User?): ViewModel() {

    fun getUser() = user
}

