package space.narrate.waylan.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

import space.narrate.waylan.core.util.MathUtils

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MathUtilsTest {

    @Test
    fun normalize_belowBounds_shouldReturnMin() {
        val result = MathUtils.normalize(
            5F,
            10F,
            20F,
            100F,
            200F
        )

        assertThat(result).isEqualTo(100F)
    }

    @Test
    fun normalize_aboveBounds_shouldReturnMax() {
        val result = MathUtils.normalize(
            25F,
            10F,
            20F,
            100F,
            200F
        )

        assertThat(result).isEqualTo(200F)
    }

    @Test
    fun normalize_scalesValue() {
        val result = MathUtils.normalize(
            15F,
            10F,
            20F,
            100F,
            200F
        )

        assertThat(result).isEqualTo(150F)
    }

    @Test
    fun normalize_reverseScalesValue() {
        val result = MathUtils.normalize(
            15F,
            10F,
            20F,
            200F,
            100F
        )

        assertThat(result).isEqualTo(150F)
    }

    @Test
    fun normalize_negativeScalesValue() {
        val result = MathUtils.normalize(
            -15F,
            -20F,
            -10F,
            100F,
            200F
        )

        assertThat(result).isEqualTo(150F)
    }
}
