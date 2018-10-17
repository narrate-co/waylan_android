package com.words.android.util

import android.app.Activity
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun <V: View?> BottomSheetBehavior<V>.collapse(activity: Activity? = null): Boolean {
    if (state != BottomSheetBehavior.STATE_HIDDEN && state != BottomSheetBehavior.STATE_COLLAPSED) {
        state = BottomSheetBehavior.STATE_COLLAPSED
        activity?.hideSoftKeyboard()
        return true
    }
    return false
}