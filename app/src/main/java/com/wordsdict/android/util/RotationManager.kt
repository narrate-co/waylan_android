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

    data class OrientationPair(val old: Int, val new: Int)

    private val lastDispatchedOrientations: HashMap<String, OrientationPair> = hashMapOf()
    private val lastUndispatchedOrientations: HashMap<String, Int> = hashMapOf()
    private val ownerObservers: HashSet<Pair<LifecycleOwner?, Observer>> = hashSetOf()

    private var orientation: OrientationPair = OrientationPair(ORIENTATION_UNSET, ORIENTATION_UNSET)

    init {
        enable()
    }

    fun observe(key: String, owner: LifecycleOwner, observer: Observer): RotationManager {
        val pair = Pair(owner, observer)

        //check if the observer has previously subscribed and has a last state
        if (lastUndispatchedOrientations.containsKey(key)) {
            val new = orientation.new
            val old = lastUndispatchedOrientations[key] ?: new

            lastUndispatchedOrientations.remove(key)

            maybeDispatchUnconsumedOrientationChange(key, pair, old, new)
        }

        owner.lifecycle.doOnDestroy {
            lastUndispatchedOrientations[key] = orientation.old
            ownerObservers.remove(pair)
        }
        ownerObservers.add(pair)
        return this
    }

    fun observeForever(observer: Observer): RotationManager {
        ownerObservers.add(Pair(null, observer))
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
            maybeDispatchRotationChanged(old, or)
        }
    }

    private fun maybeDispatchRotationChanged(old: Int, new: Int) {
        //Only send if the app is locked but not the OS. Respect the global OS lock by not triggering a rotation change
        if (appOrientationLocked() && !systemSettingsOrientationLocked()) {
            ownerObservers.forEach {
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


    private fun maybeDispatchUnconsumedOrientationChange(key: String, ownerObserver: Pair<LifecycleOwner?, Observer>, old: Int, new: Int) {
        val lastDispatch = lastDispatchedOrientations[key]
        if (lastDispatch?.old == old && lastDispatch.new == new) {
            //this is the exact same dispatch.
            //it's likely that a different type of configuration change has happened and caused the lifecycle owner to be recreated
            return
        } else if (ownerObserver.first?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
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