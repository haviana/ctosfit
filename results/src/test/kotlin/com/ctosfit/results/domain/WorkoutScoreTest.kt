package com.ctosfit.results.domain

import java.math.BigDecimal
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutScoreTest {
    @Test
    fun `load retains explicit unit`() {
        val score = LoadScore(BigDecimal("102.5"), LoadUnit.KILOGRAM)

        assertEquals(BigDecimal("102.5"), score.amount)
        assertEquals(LoadUnit.KILOGRAM, score.unit)
    }

    @Test
    fun `rejects negative score values`() {
        assertFailsWith<IllegalArgumentException> {
            LoadScore(BigDecimal("-1"), LoadUnit.POUND)
        }
        assertFailsWith<IllegalArgumentException> {
            TimeScore(Duration.ofSeconds(-1), TimeOutcome.COMPLETED)
        }
        assertFailsWith<IllegalArgumentException> { RepsScore(-1) }
        assertFailsWith<IllegalArgumentException> { RoundsAndRepsScore(1, -1) }
        assertFailsWith<IllegalArgumentException> {
            DistanceScore(BigDecimal("-0.1"), DistanceUnit.METER)
        }
        assertFailsWith<IllegalArgumentException> { CaloriesScore(-1) }
    }
}
