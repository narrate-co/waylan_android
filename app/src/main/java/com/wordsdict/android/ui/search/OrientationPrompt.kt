package com.wordsdict.android.ui.search

import com.wordsdict.android.R
import com.wordsdict.android.data.prefs.Orientation

//enum class OrientationPrompt(val message: Int, val icon: Int) {
//    LOCK_TO_LANDSCAPE(R.string.orientation_prompt_lock_to_landscape, R.drawable.ic_round_orientation_lock_24px),
//    LOCK_TO_PORTRAIT(R.string.orientation_prompt_lock_to_portrait, R.drawable.ic_round_orientation_lock_24px),
//    UNLOCK_ORIENTATION(R.string.orientation_prompt_unlock_orientation, R.drawable.ic_round_orientation_lock_24px)
//}

sealed class OrientationPrompt(val orientationToRequest: Orientation, val message: Int, val icon: Int) {
    class LockToLandscape(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_lock_to_landscape, R.drawable.ic_round_orientation_lock_24px)
    class LockToPortrait(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_lock_to_portrait, R.drawable.ic_round_orientation_lock_24px)
    class UnlockOrientation(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_unlock_orientation, R.drawable.ic_round_orientation_lock_24px)
}


