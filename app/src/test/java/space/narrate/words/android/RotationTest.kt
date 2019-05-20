package space.narrate.words.android

import android.content.pm.ActivityInfo
import space.narrate.words.android.data.prefs.RotationManager
import org.junit.Test
import space.narrate.words.android.data.prefs.RotationUtils

class RotationTest {

    @Test
    fun should_meet_basic_scenario_and_match_pattern() {
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
    fun should_not_meet_basic_scenario() {
        // Time we subscribe to RotationManager
        val observedSince = System.nanoTime()
        val now: Long = (observedSince +
                (RotationUtils.FRESH_INTERACTION_ROTATION_ACCLAMATION_PERIOD_NANOS * .75F)).toLong()

        //A list of rotation events that all happen with less than 1500 millis apart and
        // between observedSince and now
        val lockedTo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val patterns = RotationManager.PATTERN_L_P_L.mapIndexed { index, i ->
            RotationManager.RotationEvent(i, observedSince - index)
        }

        //An observeSince time that's qualifies
        assert(!RotationUtils.isLikelyUnlockDesiredScenario(patterns, lockedTo, observedSince, now))
    }
}