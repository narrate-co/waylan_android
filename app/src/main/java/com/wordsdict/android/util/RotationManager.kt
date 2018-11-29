package com.wordsdict.android.util

import android.content.Context
import android.content.pm.ActivityInfo
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.Preferences
import com.wordsdict.android.ui.search.TrimmedStack
import com.wordsdict.android.ui.search.emptyTrimmedStack
import com.wordsdict.android.ui.search.hasPattern

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
    }

    interface Observer {
        fun onUnlockedOrientationChange(old: Int, new: Int)
        fun onLockedRotate(old: Int, new: Int)
    }

    interface PatternObserver {
        fun onUnlockedOrientationPatternSeen(pattern: List<Int>)
        fun onLockedRotatePatternSeen(pattern: List<Int>)
    }

    data class OwnedPatternObserver(
            val owner: LifecycleOwner,
            val patternObserver: PatternObserver,
            val pattern: List<Int>
    )

//    private val lastDispatchedPattern: HashMap<String, List<Int>> = hashMapOf()
    private val lastUndispatchedPatterns: HashMap<String, TrimmedStack<Int>> = hashMapOf()
    private val ownedPatternObservers: HashSet<OwnedPatternObserver> = hashSetOf()

    data class OrientationPair(val old: Int, val new: Int)

    private val lastDispatchedOrientations: HashMap<String, OrientationPair> = hashMapOf()
    //TODO do we need this or can we use [orientation]?
    private val lastUndispatchedOrientations: HashMap<String, Int> = hashMapOf()

    private val ownedObservers: HashSet<Pair<LifecycleOwner?, Observer>> = hashSetOf()

    private var orientation: OrientationPair = OrientationPair(ORIENTATION_UNSET, ORIENTATION_UNSET)

    private val history = emptyTrimmedStack<Int>(4)

    init {
        enable()
    }

    fun observe(key: String, owner: LifecycleOwner, observer: Observer) {
        val pair = Pair(owner, observer)

        //check if the observer has previously subscribed and has a last state
        if (lastUndispatchedOrientations.containsKey(key)) {
            val new = orientation.new
            val old = lastUndispatchedOrientations[key] ?: new

            lastUndispatchedOrientations.remove(key)

            maybeDispatchUnconsumedOrientationChange(key, pair, old, new)
        }

        owner.lifecycle.doOnDestroy {
            lastUndispatchedOrientations[key] = orientation.old //TODO should this be moved into onPause?
            ownedObservers.remove(pair)
        }

        ownedObservers.add(pair)
    }

    fun observeForPattern(key: String, owner: LifecycleOwner, pattern: List<Int>, patternObserver: PatternObserver) {
        val ownedPatternObserver = OwnedPatternObserver(owner, patternObserver, pattern)

        //if the current pattern matches our target pattern, dispatch immediately unless it was the last pattern to be dispatched
        //TODO dispatch undispatached pattern?
        val lastUndispatchedPattern = lastUndispatchedPatterns[key]
        if (lastUndispatchedPattern != null) {
            lastUndispatchedPatterns.remove(key)

            maybeDispatchUnconsumedPattern(ownedPatternObserver, lastUndispatchedPattern)
        }

        owner.lifecycle.doOnDestroy {
            //TODO save undispatched pattern?
            lastUndispatchedPatterns[key] = history
            ownedPatternObservers.remove(ownedPatternObserver)
        }

        ownedPatternObservers.add(ownedPatternObserver)
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

        if (or != orientation.new) {
            val old = orientation.new
            orientation = OrientationPair(old, or)
            history.push(or)
            maybeDispatchRotationChanged(old, or)
        }
    }

    /**
     * Sends rotation changes to listeners only if the apps orientation is locked
     */
    private fun maybeDispatchRotationChanged(old: Int, new: Int) {
        //Only send if the app is locked but not the OS. Respect the global OS lock by not triggering a rotation change
        if (appOrientationLocked() && !systemSettingsOrientationLocked()) {


            // invoke pattern seen listeners
            ownedPatternObservers.forEach {
                if (history.hasPattern(it.pattern)) {
                    if (it.owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        it.patternObserver.onLockedRotatePatternSeen(it.pattern)
                    }
                }
            }

            // invoke rotate listeners
            ownedObservers.forEach {
                val lifecycle = it.first?.lifecycle
                val observer = it.second
                if (lifecycle == null) {
                    // this observer has no lifecycle. we're observing forever
                    observer.onLockedRotate(old, new)
                } else if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    // this observer is resumed. call its listener
                    observer.onLockedRotate(old, new)
                }

                // this observer is not resumed and not ready to consume this event. do nothing
            }
        }

        //otherwise, the app will rotate and the orientation change will be picked up by the activity
    }


    /**
     * Only called when an observer is re-subscribed after an orientation change
     */
    private fun maybeDispatchUnconsumedOrientationChange(key: String, ownerObserver: Pair<LifecycleOwner?, Observer>, old: Int, new: Int) {

        //handle dispatching single orientation change
        println("maybeDispatchUnconsumedOrientationChange - orientation: $orientation, old: $old, new: $new")
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

    private fun dispatchUnconsumedOrientationChange(key: String, ownerObserver: Pair<LifecycleOwner?, Observer>, old: Int, new: Int) {
        lastDispatchedOrientations[key] = OrientationPair(old, new)
        ownerObserver.second.onUnlockedOrientationChange(old, new)
    }

    private fun maybeDispatchUnconsumedPattern(ownedPatternObserver: OwnedPatternObserver, lastUndispatchedPattern: TrimmedStack<Int>) {
        if (lastUndispatchedPattern.hasPattern(ownedPatternObserver.pattern)) {
            if (ownedPatternObserver.owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                dispatchUnconsumedPattern(ownedPatternObserver)
            } else {
                ownedPatternObserver.owner.lifecycle.doOnNextResume {
                    dispatchUnconsumedPattern(ownedPatternObserver)
                }
            }
        }
    }

    private fun dispatchUnconsumedPattern(ownedPatternObserver: OwnedPatternObserver) {
        ownedPatternObserver.patternObserver.onUnlockedOrientationPatternSeen(ownedPatternObserver.pattern)
    }


    private fun appOrientationLocked(): Boolean {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.name) != Orientation.UNSPECIFIED.name
    }


    private fun systemSettingsOrientationLocked(): Boolean {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) != 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}