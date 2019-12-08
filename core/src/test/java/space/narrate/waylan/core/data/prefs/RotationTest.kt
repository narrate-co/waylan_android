package space.narrate.waylan.core.data.prefs

import android.content.pm.ActivityInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RotationTest {

    @Test
    fun shouldMeetBasicScenarioAndMatchPattern() {
        // Time we subscribe to RotationManager
        val observedSince = System.nanoTime()
        val now: Long = (observedSince +
                (RotationUtils.FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS * .75F)).toLong()

        //A list of rotation events that all happen with less than 1500 millis apart and
        // between observedSince and now
        val lockedTo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val patterns = RotationManager.PATTERN_L_P_L.mapIndexed { index, i ->
            RotationManager.RotationEvent(i, (now - ((now - observedSince) / 2)) + index)
        }

        //An observeSince time that's qualifies
        assert(RotationUtils.isLikelyUnlockDesiredScenario(patterns, lockedTo, observedSince, now))
    }

    @Test
    fun shouldExceedValidRotationEventPeriod() {

        // Configure a valid start and end observation period
        val observedSince = System.nanoTime()
        val now = (observedSince +
            (RotationUtils.FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS * .99F)).toLong()

        val lockedTo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val invalidEventPeriod = RotationUtils.MAX_PERIOD_BETWEEN_ROTATIONS_FOR_VALID_PATTERN_NANOS + 1L
        val patterns = RotationManager.PATTERN_L_P_L.mapIndexed { index, pattern ->
            val timeStamp = when (index) {
                0 -> observedSince
                else -> observedSince + index + invalidEventPeriod
            }
            RotationManager.RotationEvent(pattern, timeStamp)
        }

        assertThat(
            RotationUtils.isLikelyUnlockDesiredScenario(patterns, lockedTo, observedSince, now)
        ).isFalse()
    }

    @Test
    fun shouldExceedValidAcclamationPeriod() {

        val observedSince = System.nanoTime()
        val now = observedSince + (RotationUtils.FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS + 1L)

        val lockedTo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val patterns = RotationManager.PATTERN_L_P_L.mapIndexed { index, pattern ->
            RotationManager.RotationEvent(pattern, observedSince + index)
        }

        assertThat(
            RotationUtils.isLikelyUnlockDesiredScenario(patterns, lockedTo, observedSince, now)
        ).isFalse()
    }
}