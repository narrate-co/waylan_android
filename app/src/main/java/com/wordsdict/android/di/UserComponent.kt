package com.wordsdict.android.di

import com.google.firebase.auth.FirebaseUser
import com.wordsdict.android.App
import com.wordsdict.android.data.firestore.users.User
import dagger.BindsInstance
import dagger.Subcomponent

@UserScope
@Subcomponent(modules = [UserModule::class])
interface UserComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun user(user: User?): UserComponent.Builder
        @BindsInstance
        fun firebaseUser(firebaseUser: FirebaseUser?): UserComponent.Builder
        fun build(): UserComponent
    }

    fun inject(app: App)
}

