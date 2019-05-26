package space.narrate.words.android.util

import space.narrate.words.android.util.peekOrNull
import org.junit.Test
import java.util.*
import org.junit.Assert.*

class UtilTest {

    @Test
    fun emptyStack_peekShouldReturnNull() {
        val stack = Stack<Int>()

        assertEquals(null, stack.peekOrNull)
    }

    @Test
    fun nonEmptyStack_shouldReturnLastValue() {
        val stack = Stack<Int>()
        stack.push(1)

        assertEquals(1, stack.peekOrNull)
    }
}