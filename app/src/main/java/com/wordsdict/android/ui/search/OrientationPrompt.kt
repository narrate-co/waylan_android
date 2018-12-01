package com.wordsdict.android.ui.search

import android.content.pm.ActivityInfo
import com.wordsdict.android.R
import com.wordsdict.android.data.prefs.Orientation

sealed class OrientationPrompt(val orientationToRequest: Orientation, val message: Int, val icon: Int, val checkedText: Int, val uncheckedText: Int) {
    class LockToLandscape(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_lock_to_landscape, R.drawable.ic_round_orientation_lock_24px, R.string.orientation_prompt_locked_label, R.string.orientation_prompt_unlocked_label)
    class LockToPortrait(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_lock_to_portrait, R.drawable.ic_round_orientation_lock_24px, R.string.orientation_prompt_locked_label, R.string.orientation_prompt_unlocked_label)
    class UnlockOrientation(orientationToRequest: Orientation): OrientationPrompt(orientationToRequest, R.string.orientation_prompt_unlock_orientation, R.drawable.ic_round_orientation_lock_24px, R.string.orientation_prompt_unlocked_label, R.string.orientation_prompt_locked_label)
}


