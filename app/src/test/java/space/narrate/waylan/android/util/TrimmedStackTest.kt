package space.narrate.waylan.android.util

import space.narrate.waylan.android.data.prefs.RotationManager
import org.junit.Test
import org.junit.Assert.*

class TrimmedStackTest {

    companion object {
        private const val CAPACITY = 3
    }

    @Test
    fun empty_initializer_creates_null_stack() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY)
        trimmedStack.forEach {
            assertEquals(null, it)
        }
    }

    @Test
    fun push_adds_to_first_index() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY)
        trimmedStack.push(20)
        trimmedStack.forEachIndexed { index, i ->
            if (index == 0) {
                assertEquals(20, i)
            } else {
                assertEquals(null, i)
            }
        }
    }

    @Test
    fun full_stack_rotates() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY) //[null, null, null]

        trimmedStack.push(4) // [4, null, null]
        trimmedStack.push(5) // [5, 4, null]
        trimmedStack.push(6) // [6, 5, 4]

        val expected = listOf(6,5,4)
        trimmedStack.forEachIndexed { index, i ->
            assertEquals(expected[index], i)
        }
    }

    @Test
    fun to_string_prints_reverse_order() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY)

        trimmedStack.push(4) // [4, null, null]
        trimmedStack.push(5) // [5, 4, null]
        trimmedStack.push(6) // [6, 5, 4]

        val expected = listOf(4,5,6).toString()
        assertEquals(expected, trimmedStack.toString())
    }

    @Test
    fun has_pattern_matches() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY + 4)

        val landscape = 0
        val portrait = 1
        val reverseLandscape = 8

        val pattern = listOf(landscape, portrait, reverseLandscape)

        trimmedStack.push(landscape)
        trimmedStack.push(portrait)
        trimmedStack.push(reverseLandscape)

        assert(trimmedStack.hasPattern(pattern))
    }

    @Test
    fun does_not_have_pattern_stack_not_met() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY + 4)

        val landscape = 0
        val portrait = 1
        val reverseLandscape = 8

        val pattern = listOf(landscape, portrait, reverseLandscape)

        trimmedStack.push(landscape)
        trimmedStack.push(portrait)

        assert(!trimmedStack.hasPattern(pattern))
    }


    @Test
    fun does_not_have_pattern_stack_different() {
        val trimmedStack = emptyTrimmedStack<Int>(CAPACITY + 4)

        val landscape = 0
        val portrait = 1
        val reverseLandscape = 8

        val pattern = listOf(landscape, portrait, reverseLandscape)

        trimmedStack.push(landscape)
        trimmedStack.push(portrait)
        trimmedStack.push(landscape)

        assert(!trimmedStack.hasPattern(pattern))
    }

    @Test
    fun mixed_comparators_copys_on_equal() {
        val trimmedStack = emptyTrimmedStack<RotationManager.RotationEvent>(CAPACITY + 4)

        val landscape = 0
        val portrait = 1
        val reverseLandscape = 8

        val pattern = listOf(landscape, portrait, reverseLandscape)

        trimmedStack.push(RotationManager.RotationEvent(landscape, System.currentTimeMillis()))
        trimmedStack.push(RotationManager.RotationEvent(portrait, System.currentTimeMillis()))
        trimmedStack.push(RotationManager.RotationEvent(reverseLandscape, System.currentTimeMillis()))

        val copy = trimmedStack.copyMatchedPattern(pattern) { t, p ->
            t.orientation == p
        }

        assertNotNull(copy)
    }

    @Test
    fun mixed_comparators_returns_null_on_copy_no_match() {
        val trimmedStack = emptyTrimmedStack<RotationManager.RotationEvent>(CAPACITY + 4)

        val landscape = 0
        val portrait = 1
        val reverseLandscape = 8

        val pattern = listOf(landscape, portrait, reverseLandscape)

        trimmedStack.push(RotationManager.RotationEvent(landscape, System.currentTimeMillis()))
        trimmedStack.push(RotationManager.RotationEvent(portrait, System.currentTimeMillis()))

        val copy = trimmedStack.copyMatchedPattern(pattern) { t, p ->
            t.orientation == p
        }

        assertNull(copy)
    }
}