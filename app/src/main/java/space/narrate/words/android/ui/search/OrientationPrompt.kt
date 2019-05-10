package space.narrate.words.android.ui.search

import space.narrate.words.android.R
import space.narrate.words.android.data.prefs.Orientation

/**
 * A sealed class to hold the possible "smart prompts" which can be displayed in the search shelf.
 * The search shelf is a space just above the search input field which can be expanded to display
 * contextual prompts. Orientation locking, is one such prompt.
 *
 * @property orientationToRequest the [Orientation] which clicking on this prompt should set the
 *  apps orientation [UserPreferenceRepository]
 * @property message A short message to be displayed in the expanded search shelf area
 * @property icon An icon to be displayed to the left of [message] in the expanded search shelf area
 * @property checkedText The textRes to be displayed in the search shelf after the first click on
 *  the prompt, indicating that the prompt has been "checked" or "completed"
 * @property uncheckedText The textRes to be displayed in the search shelf if the user clicks on
 *  [checkedText], indicating that they want to reverse the "checked" or "completed" prompt.
 */
sealed class OrientationPrompt(
        val orientationToRequest: Orientation,
        val message: Int,
        val icon: Int,
        val checkedText: Int,
        val uncheckedText: Int
) {

    class LockToLandscape(orientationToRequest: Orientation) : OrientationPrompt(
            orientationToRequest,
            R.string.orientation_prompt_lock_to_landscape,
            R.drawable.ic_round_orientation_lock_24px,
            R.string.orientation_prompt_locked_label,
            R.string.orientation_prompt_unlocked_label
    )

    class LockToPortrait(orientationToRequest: Orientation) : OrientationPrompt(
            orientationToRequest,
            R.string.orientation_prompt_lock_to_portrait,
            R.drawable.ic_round_orientation_lock_24px,
            R.string.orientation_prompt_locked_label,
            R.string.orientation_prompt_unlocked_label
    )

    class UnlockOrientation(orientationToRequest: Orientation) : OrientationPrompt(
            orientationToRequest,
            R.string.orientation_prompt_unlock_orientation,
            R.drawable.ic_round_orientation_lock_24px,
            R.string.orientation_prompt_unlocked_label,
            R.string.orientation_prompt_locked_label
    )
}


