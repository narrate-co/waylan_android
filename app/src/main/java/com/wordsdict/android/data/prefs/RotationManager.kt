package com.wordsdict.android.data.prefs

import android.content.Context
import android.content.pm.ActivityInfo
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.wordsdict.android.util.widget.TrimmedStack
import com.wordsdict.android.util.widget.copyMatchedPattern
import com.wordsdict.android.util.widget.emptyTrimmedStack
import com.wordsdict.android.util.doOnDestroy
import com.wordsdict.android.util.doOnNextResume
import com.wordsdict.android.util.doOnResume
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A class that reports rotation events ONLY WHEN the device or app orientation is locked
 *
 * Other rotation events are handled when the app is reconfigured in its new orientation
 */
class RotationManager constructor(
        private val context: Context
): OrientationEventListener(context) {

    companion object {
        const val ORIENTATION_UNSET = -2

        const val MAX_HISTORY_CAPACITY = 4
        const val CLEAR_ROTATION_HISTORY_DELAY = 3000L

        val PATTERN_P_RL_P = listOf(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )

        val PATTERN_P_L_P = listOf(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )

        val PATTERN_L_P_L = listOf(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        )

        val PATTERN_RL_P_RL = listOf(
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        )

        val PATTERNS_ALL = hashSetOf(
                PATTERN_L_P_L, PATTERN_P_L_P, PATTERN_P_RL_P, PATTERN_RL_P_RL
        )

    }

    interface Observer {
        fun onUnlockedOrientationChange(old: RotationEvent, new: RotationEvent)
        fun onLockedRotate(old: RotationEvent, new: RotationEvent, lockedTo: Int)
    }

    interface PatternObserver {
        fun onUnlockedOrientationPatternSeen(pattern: List<RotationEvent>)
        fun onLockedRotatePatternSeen(pattern: List<RotationEvent>, lockedTo: Int, observedSince: Long)
    }

    data class RotationEvent(val orientation: Int, val timeStamp: Long) {

        override fun equals(other: Any?): Boolean {
            if (other !is RotationEvent) return false

            return orientation == other.orientation
        }
    }

    data class OwnedPatternObserver(
            val owner: LifecycleOwner,
            var observedSince: Long,
            val patternObserver: PatternObserver,
            val patterns: Set<List<Int>>
    )

    private val lastUndispatchedPatterns: HashMap<String, TrimmedStack<RotationEvent>> = hashMapOf()

    private val ownedPatternObservers: HashMap<String, OwnedPatternObserver> = hashMapOf()

    data class OrientationPair(val old: RotationEvent, val new: RotationEvent)

    private val lastDispatchedOrientations: HashMap<String, OrientationPair> = hashMapOf()
    //TODO do we need this or can we use [orientation]?
    private val lastUndispatchedOrientations: HashMap<String, RotationEvent> = hashMapOf()

    private val ownedObservers: HashSet<Pair<LifecycleOwner?, Observer>> = hashSetOf()

    private var rotation: OrientationPair = OrientationPair(RotationEvent(ORIENTATION_UNSET, timeStamp()), RotationEvent(ORIENTATION_UNSET, timeStamp()))

    private val history = emptyTrimmedStack<RotationEvent>(MAX_HISTORY_CAPACITY)

    private var clearHistoryOnDelayJob: Job? = null

    init {
        enable()
    }

    private fun timeStamp() = System.nanoTime()

    fun observe(key: String, owner: LifecycleOwner, observer: Observer) {
        val pair = Pair(owner, observer)

        //check if the observer has previously subscribed and has a last state
        if (lastUndispatchedOrientations.containsKey(key)) {
            val new = rotation.new
            val old = lastUndispatchedOrientations[key] ?: new

            lastUndispatchedOrientations.remove(key)

            maybeDispatchUnconsumedOrientationChange(key, pair, old, new)
        }

        owner.lifecycle.doOnDestroy {
            lastUndispatchedOrientations[key] = rotation.old //TODO should this be moved into onPause?
            ownedObservers.remove(pair)
        }

        ownedObservers.add(pair)
    }

    fun observeForPattern(key: String, owner: LifecycleOwner, patterns: Set<List<Int>>, patternObserver: PatternObserver) {
        val ownedPatternObserver = OwnedPatternObserver(owner, timeStamp(), patternObserver, patterns)

        //if the current pattern matches our target pattern, dispatch immediately unless it was the last pattern to be dispatched
        val lastUndispatchedPattern = lastUndispatchedPatterns[key]
        if (lastUndispatchedPattern != null) {
            lastUndispatchedPatterns.remove(key)


            maybeDispatchUnconsumedPattern(ownedPatternObserver, lastUndispatchedPattern)
        }

        owner.lifecycle.doOnDestroy {
            lastUndispatchedPatterns[key] = history
            ownedPatternObservers.remove(key)
        }

        owner.lifecycle.doOnResume {
            ownedPatternObservers[key]?.observedSince = timeStamp()
        }

        ownedPatternObservers[key] = ownedPatternObserver
    }


    fun observeForever(observer: Observer): RotationManager {
        ownedObservers.add(Pair(null, observer))
        return this
    }

    override fun onOrientationChanged(degrees: Int) {
        if (degrees == ORIENTATION_UNKNOWN) return

        val or = when (degrees) {
            in 45..134 -> {
                // reverse landscape
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            in 135..224 -> {
                // reverse portrait not supported
                return
            }
            in 225..314 -> {
                // landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            else -> {
                // portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        if (or != rotation.new.orientation) {
            val old = rotation.new
            val new = RotationEvent(or, timeStamp())
            rotation = OrientationPair(old, new)
            history.push(new)
            maybeDispatchRotationChanged(old, new)
            //TODO set a timeout to clear [history] to only capture rotation events that happen in succession
            clearHistoryOnDelayJob?.cancel()
            clearHistoryOnDelayJob = launch {
                delay(CLEAR_ROTATION_HISTORY_DELAY)
                history.clear()
                history.push(RotationEvent(rotation.new.orientation, timeStamp()))
            }
        }
    }

    /**
     * Sends rotation changes to listeners only if the apps orientation is locked
     */
    private fun maybeDispatchRotationChanged(old: RotationEvent, new: RotationEvent) {
        //Only send if the app is locked but not the OS. Respect the global OS lock by not triggering a rotation change
        if (appIsOrientationLocked() && !systemIsOrientationLocked()) {


            // invoke pattern seen listeners
            ownedPatternObservers.values.forEach {owner ->
                owner.patterns.forEach {pattern ->
                    val copy = history.copyMatchedPattern(pattern) { rotationEvent, orientation ->
                        rotationEvent.orientation == orientation
                    }
                    if (copy != null) {
                        if (owner.owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            owner.patternObserver.onLockedRotatePatternSeen(copy, appOrientationLock, owner.observedSince)
                        }
                    }
                }
            }

            // invoke rotate listeners
            ownedObservers.forEach {
                val lifecycle = it.first?.lifecycle
                val observer = it.second
                if (lifecycle == null) {
                    // this observer has no lifecycle. we're observing forever
                    observer.onLockedRotate(old, new, appOrientationLock)
                } else if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    // this observer is resumed. call its listener
                    observer.onLockedRotate(old, new, appOrientationLock)
                }

                // this observer is not resumed and not ready to consume this event. do nothing
            }
        }

        //otherwise, the app will rotate and the orientation change will be picked up by the activity
    }


    /**
     * Only called when an observer is re-subscribed after an orientation change
     */
    private fun maybeDispatchUnconsumedOrientationChange(key: String, ownerObserver: Pair<LifecycleOwner?, Observer>, old: RotationEvent, new: RotationEvent) {

        //handle dispatching single orientation change
        println("maybeDispatchUnconsumedOrientationChange - orientation: $rotation, old: $old, new: $new")
        val lastDispatch = lastDispatchedOrientations[key]
        if (lastDispatch?.old == old && lastDispatch.new == new) {
            //this is the exact same dispatch.
            //it's likely that a different type of configuration change has happened and caused the lifecycle owner to be recreated
            return
        }


        if (ownerObserver.first?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            //the activity has already called onResume. dispatch orientation change ourselves
            dispatchUnconsumedOrientationChange(key, ownerObserver, old, new)
        } else {
            //wait for onResume to be called and then dispatch
            ownerObserver.first?.lifecycle?.doOnNextResume {
                dispatchUnconsumedOrientationChange(key, ownerObserver, old, new)
            }
        }
    }

    private fun dispatchUnconsumedOrientationChange(key: String, ownerObserver: Pair<LifecycleOwner?, Observer>, old: RotationEvent, new: RotationEvent) {
        lastDispatchedOrientations[key] = OrientationPair(old, new)
        ownerObserver.second.onUnlockedOrientationChange(old, new)
    }

    private fun maybeDispatchUnconsumedPattern(ownedPatternObserver: OwnedPatternObserver, lastUndispatchedPattern: TrimmedStack<RotationEvent>) {
        ownedPatternObserver.patterns.forEach { pattern ->

            val copy = lastUndispatchedPattern.copyMatchedPattern(pattern) { event, orientation ->
                event.orientation == orientation
            }
            if (copy != null) {
                if (ownedPatternObserver.owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    dispatchUnconsumedPattern(ownedPatternObserver, copy)
                } else {
                    ownedPatternObserver.owner.lifecycle.doOnNextResume {
                        dispatchUnconsumedPattern(ownedPatternObserver, copy)
                    }
                }
            }
        }
    }

    private fun dispatchUnconsumedPattern(ownedPatternObserver: OwnedPatternObserver, pattern: List<RotationEvent>) {
        ownedPatternObserver.patternObserver.onUnlockedOrientationPatternSeen(pattern)
    }

    private val appOrientationLock: Int
        get() = PreferenceManager.getDefaultSharedPreferences(context).getInt(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.value)

    private fun appIsOrientationLocked(): Boolean {
        return appOrientationLock != Orientation.UNSPECIFIED.value
    }

    private fun systemIsOrientationLocked(): Boolean {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) != 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}