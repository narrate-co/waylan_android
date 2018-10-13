package com.words.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseUser
import com.words.android.data.firestore.User
import com.words.android.di.AppInjector
import com.words.android.di.UserComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App: Application(), HasActivityInjector {


    companion object {
        const val REINJECT_USER_BROADCAST_ACTION = "reinject_user_broadcast_action"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var userComponentBuilder: UserComponent.Builder

    private var user: FirebaseUser? = null

    fun setUser(user: User?) {
        userComponentBuilder
                .user(user)
                .build()
                .inject(this)
        dispatchReinjectUserBroadcast()
    }

    fun clearUser() {
        user = null
        AppInjector.init(this)
        dispatchReinjectUserBroadcast()
    }

    private fun dispatchReinjectUserBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(REINJECT_USER_BROADCAST_ACTION))
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector









//
//    private val appDatabase: AppDatabase by lazy { AppDatabase.getInstance(this) }
//
//    lateinit var wordRepository: WordRepository
//
//    val viewModelFactory: ViewModelFactory by lazy { ViewModelFactory(this) }
//
//    /**
//     * user is set from AuthActivity.
//     * if user is null, we're operating in 'guest' mode and all firebase enabled functionality
//     * should be disallowed. This would only occur if the user doesn't have internet to
//     * sign in anonymously and should rarely be the case.
//     */
//    var user: User? = null
//        set(value) {
//            field = value
//
//            if (value?.user != null) {
//                firestoreStore = FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, value.user)
//            } else {
//                firestoreStore = null
//            }
//
//            if (value?.isMerriamWebsterSubscriber == true) {
//                merriamWebsterStore = MerriamWebsterStore(RetrofitService.getInstance(), appDatabase.mwDao())
//            } else {
//                merriamWebsterStore = null
//            }
//
//            wordRepository = WordRepository(appDatabase, firestoreStore, merriamWebsterStore)
//        }
//
//    var firestoreStore: FirestoreStore? = null
//
//    var merriamWebsterStore: MerriamWebsterStore? = null

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
//        appDatabase.init()
//        wordRepository = WordRepository(appDatabase, firestoreStore, merriamWebsterStore)
    }


}

