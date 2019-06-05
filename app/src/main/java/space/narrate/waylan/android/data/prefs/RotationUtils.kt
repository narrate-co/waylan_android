package space.narrate.waylan.android.data.prefs

import android.content.pm.ActivityInfo
import java.util.concurrent.TimeUnit

/**
 * A collection of helper objects and functions to determine the listType and validity of rotations
 * and rotation events
 */

object RotationUtils {
    // imagine a user opens the app. If the orientation is locked and they'd like otherwise, it's
    // likely they'll rotate the device and start hitting patterns.
    // [POST_OBSERVE_INITIAL_ACCLAMATION_PERIOD] is the allowance we're giving the user to do that
    val FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS =
            TimeUnit.MILLISECONDS.toNanos(5000L)

    // If the time between a rotation pattern is too long, it's unlikely the user is
    // rotating on purpose or to any desired result
    val MAX_PERIOD_BETWEEN_ROTATIONS_FOR_VALID_PATTERN_NANOS =
            TimeUnit.MILLISECONDS.toNanos(3000L)

    fun isPortraitToLandscape(
            old: RotationManager.RotationEvent,
            new: RotationManager.RotationEvent
    ): Boolean {
        return (old.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || old.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
                && (new.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                || new.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }

    fun isLandscapeToPortrait(
            old: RotationManager.RotationEvent,
            new: RotationManager.RotationEvent
    ): Boolean {
        return (old.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                || old.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                && (new.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || new.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
    }


    fun isLikelyUnlockDesiredScenario(
            pattern: List<RotationManager.RotationEvent>,
            lockedTo: Int,
            observedSince: Long, now: Long
    ): Boolean {

        var maxPeriodBetweenRotations: Long = -1L
        pattern.forEachIndexed { index, rotationEvent ->
            if (pattern.lastIndex >= index + 1) {
                maxPeriodBetweenRotations = Math.max(
                        maxPeriodBetweenRotations,
                        pattern[index + 1].timeStamp - rotationEvent.timeStamp
                )
            }
        }

        val rotationEventPeriodsQualifies
                = maxPeriodBetweenRotations <= MAX_PERIOD_BETWEEN_ROTATIONS_FOR_VALID_PATTERN_NANOS
        val acclamationPeriodQualifies =
                now - observedSince <= FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS

        if (!rotationEventPeriodsQualifies || !acclamationPeriodQualifies) return false

        //when locked to portrait
        ////should be true for l-p-l && rl-p-rl
        val orientations = pattern.map { it.orientation }
        if (lockedTo == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (orientations == RotationManager.PATTERN_L_P_L) return true
            if (orientations == RotationManager.PATTERN_RL_P_RL) return true
        }

        //when locked to landscape
        ////should be true for p-l-p
        if (lockedTo == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            if (orientations == RotationManager.PATTERN_P_L_P) return true
        }

        //when locked to reverse landscape
        ////should be true for p-rl-p
        if (lockedTo == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            if (orientations == RotationManager.PATTERN_P_RL_P) return true
        }

        return false
    }
}

