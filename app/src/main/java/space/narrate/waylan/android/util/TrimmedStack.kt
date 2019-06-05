package space.narrate.waylan.android.util

import java.lang.StringBuilder
import java.util.*

/**
 * A class that holds a set number of items and maintains a chronological list from
 * most recently pushed to least recently pushed
 */
class TrimmedStack<T>(private var array: MutableList<T?>): Iterable<T?> {

    /**
     * @param capacity the max number of items which should be held. Adding new items will
     *  remove the oldest.
     * @param init a function used to create each initial item in the stack
     */
    constructor(capacity: Int, init: (index: Int) -> T?) : this(MutableList(capacity, init))

    val capacity: Int = array.size

    fun push(t: T) {
        Collections.rotate(array, 1)
        array[0] = t
    }

    fun peek(): T? {
        return array[0]
    }

    fun clear() {
        array = MutableList(capacity) { null }
    }

    inner class TrimmedStackIterator(startFrom: Int = 0): Iterator<T?> {
        private var current: Int = startFrom

        override fun hasNext(): Boolean {
            return array.size > current
        }

        override fun next(): T? {
            val i = current
            current++
            return array[i]
        }
    }

    override fun iterator(): Iterator<T?> {
        return TrimmedStackIterator()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.insert(0, "]")
        forEachIndexed { index, t ->
            if (index == 0) {
                sb.insert(0, t.toString())
            } else {
                sb.insert(0, "$t, ")
            }
        }
        sb.insert(0, "[")
        return sb.toString()
    }
}

fun <T> trimmedStackOf(vararg elements: T): TrimmedStack<T> =
        if (elements.size > 0) {
            TrimmedStack<T>(elements.asList().toMutableList())
        } else {
            TrimmedStack(0) { null }
        }

fun <T> emptyTrimmedStack(capacity: Int): TrimmedStack<T> = TrimmedStack(capacity) { null }

/**
 * Determine whether the given pattern matches the most recently pushed items in this TrimmedStack
 *
 * @param pattern The pattern to match in chronological order from least recent to most recent
 *
 * pattern -> portrait, landscape, reverse portrait
 * matching stack -> [x,x,x portrait, landscape, reverse portrait]
 */
fun <T> TrimmedStack<T>.hasPattern(pattern: List<T>): Boolean {
    if (capacity < pattern.size) return false

    var matches = true

    forEachIndexed { index, t ->
        //only go up to the last index of pattern
        if (index > pattern.lastIndex) return@forEachIndexed

        val patternIndex = pattern.lastIndex - index
        if (t != pattern[patternIndex]) {
            matches = false
            return@forEachIndexed
        }
    }

    return matches
}

/**
 * Determine whether the given pattern matches the most recently pushed items in this TrimmedStack,
 * using [areEqual] to compare items. If the pattern is present, a list of this TrimmedStack (in
 * order of most recent to least recent) will be returned. Otherwise, null will be returned.
 *
 * @param pattern The pattern to check for in order of most recently to least recent
 * @param areEqual A function to compare items in [pattern] against items in this TrimmedStack. This
 *  is helpful if items are of different types.
 *
 * @return A list, exactly the same as [pattern], but of [T], the listType of the TrimmedStack. If
 *   the pattern is not present, null will be returned.
 */
fun <T, P> TrimmedStack<T>.copyMatchedPattern(
        pattern: List<P>,
        areEqual: (T, P) -> Boolean
): List<T>? {
    if (capacity < pattern.size) return null

    val copy: MutableList<T> = mutableListOf()

    forEachIndexed { index, t ->
        //only go up to the last index of pattern
        if (index > pattern.lastIndex) return@forEachIndexed

        val patternIndex = pattern.lastIndex - index
        if (t == null || !areEqual(t, pattern[patternIndex])) {
            return null
        } else {
            copy.add(0, t)
        }
    }

    return copy.toList()
}