package com.wordsdict.android.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsRepository(
        private val firebaseAnalytics: FirebaseAnalytics
) {

    companion object {
        private const val EVENT_SEARCH_WORD = "search_word"
        private const val EVENT_NAVIGATE_BACK = "navigate_back"
    }

    fun setUserId(uid: String?) {
        firebaseAnalytics.setUserId(uid)
    }

    fun logSearchWordEvent(input: String, selection: String, source: String) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.VALUE, input)
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, selection)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, source)
        firebaseAnalytics.logEvent(EVENT_SEARCH_WORD, params)
    }

    fun logNavigateBackEvent(stack: String, method: NavigationMethod) {
        val params = Bundle()
        params.putString(FirebaseAnalytics.Param.VALUE, stack)
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, method.name)
        firebaseAnalytics.logEvent(EVENT_NAVIGATE_BACK, params)
    }

}