package space.narrate.waylan.core.data.prefs

import android.content.Context
import android.content.pm.ActivityInfo
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import space.narrate.waylan.core.util.TrimmedStack
import space.narrate.waylan.core.util.copyMatchedPattern
import space.narrate.waylan.core.util.emptyTrimmedStack
import space.narrate.waylan.core.util.doOnDestroy
import space.narrate.waylan.core.util.doOnNextResume
import space.narrate.waylan.core.util.doOnResume
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A helper typealias definition to aid in readability. A pattern is a list of
 * [ActivityInfo] ScreenOrientation Int constants
 */
typealias Pattern = List<Int>

/**
 * Reports all orientation change or rotation events in an Activity/Fragment lifecycle friendly
 * way. When the app's screen orientation is unlocked, [RotationManager] reports
 * orientation changes <i>after</i> they've occurred. When the app's orientation is locked,
 * [RotationManager] reports rotation changes (received from [OrientationEventListener]) upon each
 * unique Orientation change.
 *
 * This class can also be set to watch for rotation <i>patterns</i>. This can be useful when it
 * makes sense to watch for the device, for example, being rotated from portrait, to landscape,
 * and back to portrait. These patterns, and their [RotationEvent.timeStamp] intervals can
 * help determine user intent and intelligently suggest things settings like orientation lock/unlock
 */
class RotationManager constructor(
        private val context: Context
): OrientationEventListener(context), CoroutineScope {

    companion object {
        // An unknown orientation
        const val ORIENTATION_UNSET = -2
        // The maximum number of RotationEvents to keep track of
        const val MAX_HISTORY_CAPACITY = 4
        // The duration to hold RotationEvents before clearing the history
        const val CLEAR_ROTATION_HISTORY_DELAY = 3000L

        // Helper definition of a [portrait, reverse_landscape, portrait] pattern
        val PATTERN_P_RL_P: Pattern = listOf(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )

        // Helper definition of a [portrait, landscape, portrait] pattern
        val PATTERN_P_L_P: Pattern = listOf(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )

        // Helper definition of a [landscape, portrait, landscape] pattern
        val PATTERN_L_P_L: Pattern = listOf(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        )

        // Helper definition of a [reverse_landscape, portrait, reverse_landscape] pattern
        val PATTERN_RL_P_RL: Pattern = listOf(
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        )

        // Helper definition of all supported n=3 rotation patterns
        val PATTERNS_ALL: Set<Pattern> = hashSetOf(
            PATTERN_L_P_L, PATTERN_P_L_P, PATTERN_P_RL_P, PATTERN_RL_P_RL
        )

    }

    /**
     * A listener that receives callbacks only when the observers [LifecycleOwner] is at least
     * in the [Lifecycle.State.RESUMED] state.
     */
    interface Observer {
        /**
         * Called when the app's orientation is not locked and after the device has been rotated
         * from [old] to [new]
         */
        fun onUnlockedOrientationChange(old: RotationEvent, new: RotationEvent)

        /**
         * Called when the app's orientation is locked and after the device has been rotated
         * from [old] to [new]. Note, this will not be called if the devices global screen rotate
         * setting is set to locked.
         */
        fun onLockedRotate(old: RotationEvent, new: RotationEvent, lockedTo: Int)
    }

    /**
     * A listener that receives callbacks after a series of orientations have been seen and when
     * the observers [LifecycleOwner] is at least in the [Lifecycle.State.RESUMED] state.
     */
    interface PatternObserver {
        /**
         * Called when the app's orientation is not locked and after the device has been rotated
         * in a series that matches [pattern]
         */
        fun onUnlockedOrientationPatternSeen(pattern: List<RotationEvent>)

        /**
         * Called when the app's orientation is locked and after the device has been rotated in
         * a series that mates [pattern].
         */
        fun onLockedRotatePatternSeen(
            pattern: List<RotationEvent>,
            lockedTo: Int,
            observedSince: Long
        )
    }

    /**
     * A class to hold changes in rotation/orientation
     *
     * @param orientation The [ActivityInfo] ScreenOrientation int of the new orientation
     * @param timeStamp The [System.nanoTime] of when the event occurred.
     */
    data class RotationEvent(val orientation: Int, val timeStamp: Long) {

        override fun equals(other: Any?): Boolean {
            return if (other !is RotationEvent)  false else orientation == other.orientation
        }
    }

    private data class OwnedPatternObserver(
        val owner: LifecycleOwner,
        var observedSince: Long,
        val patternObserver: PatternObserver,
        val patterns: Set<Pattern>
    )

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val lastUndispatchedPatterns: HashMap<String, TrimmedStack<RotationEvent>> = hashMapOf()

    private val ownedPatternObservers: HashMap<String, OwnedPatternObserver> = hashMapOf()

    data class OrientationPair(val old: RotationEvent, val new: RotationEvent)

    private val lastDispatchedOrientations: HashMap<String, OrientationPair> = hashMapOf()

    //TODO do we need this or can we use [orientation]?
    private val lastUndispatchedOrientations: HashMap<String, RotationEvent> = hashMapOf()

    private val ownedObservers: HashSet<Pair<LifecycleOwner?, Observer>> = hashSetOf()

    private var rotation: OrientationPair = OrientationPair(
        RotationEvent(ORIENTATION_UNSET, timeStamp()),
        RotationEvent(ORIENTATION_UNSET, timeStamp())
    )

    private val history = emptyTrimmedStack<RotationEvent>(MAX_HISTORY_CAPACITY)

    private var clearHistoryOnDelayJob: Job? = null

    init {
        enable()
    }

    private fun timeStamp() = System.nanoTime()

    /**
     * Register a [LifecycleOwner] to watch for rotation/orientation events.
     *
     * @param key A unique identifier for the observer. Often the simpleName of the calling class
     * @param owner The LifecycleOwner which determines when events are dispatched
     * @param observer The [Observer] to receive event callbacks
     */
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
            //TODO should this be moved into onPause?
            lastUndispatchedOrientations[key] = rotation.old

            ownedObservers.remove(pair)
        }

        ownedObservers.add(pair)
    }

    /**
     * Registers a [LifecycleOwner] to watch for rotation/orientation patterns.
     *
     * @param key A unique identifier for the observer. Often the
     *      [Class.getSimpleName] of the calling class
     * @param owner The LifecycleOwner which determines when events are dispatched
     * @param patterns A Set of [Pattern]s to receive callbacks for after they occur.
     * @param patternObserver The [Observer] to receive pattern event callbacks
     */
    fun observeForPattern(
        key: String,
        owner: LifecycleOwner,
        patterns: Set<Pattern>,
        patternObserver: PatternObserver
    ) {
        val ownedPatternObserver =
            OwnedPatternObserver(owner, timeStamp(), patternObserver, patterns)

        // if the current pattern matches our target pattern, dispatch
        // immediately unless it was the last pattern to be dispatched
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
        //Only send if the app is locked but not the OS. Respect the global OS lock by not
        // triggering a rotation change
        if (appIsOrientationLocked() && !systemIsOrientationLocked()) {


            // invoke pattern seen listeners
            ownedPatternObservers.values.forEach {owner ->
                owner.patterns.forEach {pattern ->
                    val copy = history.copyMatchedPattern(pattern) { rotationEvent, orientation ->
                        rotationEvent.orientation == orientation
                    }
                    if (copy != null &&
                            owner.owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        owner.patternObserver.onLockedRotatePatternSeen(
                                copy,
                                appOrientationLock,
                                owner.observedSince
                        )
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

        // otherwise, the app will rotate and the orientation
        // change will be picked up by the activity
    }


    /**
     * Only called when an observer is re-subscribed after an orientation change
     */
    private fun maybeDispatchUnconsumedOrientationChange(
        key: String,
        ownerObserver: Pair<LifecycleOwner?, Observer>,
        old: RotationEvent,
        new: RotationEvent
    ) {

        //handle dispatching single orientation change
        val lastDispatch = lastDispatchedOrientations[key]
        if (lastDispatch?.old == old && lastDispatch.new == new) {
            // this is the exact same dispatch.
            // it's likely that a different listType of configuration change has happened
            // and caused the lifecycle owner to be recreated
            return
        }


        if (ownerObserver.first?.lifecycle
                        ?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            //the activity has already called onResume. dispatch orientation change ourselves
            dispatchUnconsumedOrientationChange(key, ownerObserver, old, new)
        } else {
            //wait for onResume to be called and then dispatch
            ownerObserver.first?.lifecycle?.doOnNextResume {
                dispatchUnconsumedOrientationChange(key, ownerObserver, old, new)
            }
        }
    }

    private fun dispatchUnconsumedOrientationChange(
        key: String,
        ownerObserver: Pair<LifecycleOwner?, Observer>,
        old: RotationEvent,
        new: RotationEvent
    ) {
        lastDispatchedOrientations[key] = OrientationPair(old, new)
        ownerObserver.second.onUnlockedOrientationChange(old, new)
    }

    private fun maybeDispatchUnconsumedPattern(
        ownedPatternObserver: OwnedPatternObserver,
        lastUndispatchedPattern: TrimmedStack<RotationEvent>
    ) {
        ownedPatternObserver.patterns.forEach { pattern ->

            val copy = lastUndispatchedPattern.copyMatchedPattern(pattern) { event, orientation ->
                event.orientation == orientation
            }
            if (copy != null) {
                if (ownedPatternObserver.owner.lifecycle
                                .currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    dispatchUnconsumedPattern(ownedPatternObserver, copy)
                } else {
                    ownedPatternObserver.owner.lifecycle.doOnNextResume {
                        dispatchUnconsumedPattern(ownedPatternObserver, copy)
                    }
                }
            }
        }
    }

    private fun dispatchUnconsumedPattern(
        ownedPatternObserver: OwnedPatternObserver,
        pattern: List<RotationEvent>
    ) {
        ownedPatternObserver.patternObserver.onUnlockedOrientationPatternSeen(pattern)
    }

    private val appOrientationLock: Int
        get() = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.value)

    private fun appIsOrientationLocked(): Boolean =
            appOrientationLock != Orientation.UNSPECIFIED.value

    private fun systemIsOrientationLocked(): Boolean {
        return try {
            Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION
            ) != 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}