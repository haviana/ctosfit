package com.ctosfit.results.domain

import java.math.BigDecimal
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutScoreComparisonTest {
    @Test
    fun `best of selects higher values and fastest completed time`() {
        assertEquals(
            ScoredAttempt(MetricId("back-squat"), LoadScore(BigDecimal("100"), LoadUnit.KILOGRAM)),
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("back-squat"), LoadScore(BigDecimal("90"), LoadUnit.KILOGRAM)),
                    ScoredAttempt(MetricId("back-squat"), LoadScore(BigDecimal("100"), LoadUnit.KILOGRAM)),
                ),
            ),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), RepsScore(12)),
            WorkoutScoreComparison.bestOf(listOf(ScoredAttempt(MetricId("test"), RepsScore(8)), ScoredAttempt(MetricId("test"), RepsScore(12)))),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), RoundsAndRepsScore(10, 1)),
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("test"), RoundsAndRepsScore(10, 1)),
                    ScoredAttempt(MetricId("test"), RoundsAndRepsScore(9, 20)),
                ),
            ),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), DistanceScore(BigDecimal("500"), DistanceUnit.METER)),
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("test"), DistanceScore(BigDecimal("400"), DistanceUnit.METER)),
                    ScoredAttempt(MetricId("test"), DistanceScore(BigDecimal("500"), DistanceUnit.METER)),
                ),
            ),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), CaloriesScore(50)),
            WorkoutScoreComparison.bestOf(listOf(ScoredAttempt(MetricId("test"), CaloriesScore(45)), ScoredAttempt(MetricId("test"), CaloriesScore(50)))),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), CompletionScore(CompletionStatus.COMPLETED)),
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("test"), CompletionScore(CompletionStatus.NOT_COMPLETED)),
                    ScoredAttempt(MetricId("test"), CompletionScore(CompletionStatus.COMPLETED)),
                ),
            ),
        )
        assertEquals(
            ScoredAttempt(MetricId("test"), TimeScore(Duration.ofMinutes(9), TimeOutcome.COMPLETED)),
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("test"), TimeScore(Duration.ofMinutes(10), TimeOutcome.COMPLETED)),
                    ScoredAttempt(MetricId("test"), TimeScore(Duration.ofMinutes(9), TimeOutcome.COMPLETED)),
                ),
            ),
        )
    }

    @Test
    fun `rejects different score types and units`() {
        val kilograms = LoadScore(BigDecimal("100"), LoadUnit.KILOGRAM)
        assertFalse(WorkoutScoreComparison.areComparable(ScoredAttempt(MetricId("test"), kilograms), ScoredAttempt(MetricId("test"), RepsScore(100))))
        assertFalse(
            WorkoutScoreComparison.areComparable(
                ScoredAttempt(MetricId("test"), kilograms),
                ScoredAttempt(MetricId("test"), LoadScore(BigDecimal("220"), LoadUnit.POUND)),
            ),
        )
        assertFalse(
            WorkoutScoreComparison.areComparable(
                ScoredAttempt(MetricId("test"), DistanceScore(BigDecimal("1000"), DistanceUnit.METER)),
                ScoredAttempt(MetricId("test"), DistanceScore(BigDecimal("1"), DistanceUnit.KILOMETER)),
            ),
        )
        assertFalse(
            WorkoutScoreComparison.areComparable(
                ScoredAttempt(MetricId("benchmark-a"), kilograms),
                ScoredAttempt(MetricId("benchmark-b"), kilograms),
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            WorkoutScoreComparison.bestOf(
                listOf(
                    ScoredAttempt(MetricId("test"), kilograms),
                    ScoredAttempt(MetricId("test"), RepsScore(100)),
                ),
            )
        }
    }

    @Test
    fun `time comparison requires matching completed outcomes`() {
        val completed = TimeScore(Duration.ofMinutes(8), TimeOutcome.COMPLETED)
        val capped = TimeScore(Duration.ofMinutes(10), TimeOutcome.TIME_CAP)
        val dnf = TimeScore(Duration.ofMinutes(9), TimeOutcome.DID_NOT_FINISH)

        assertTrue(WorkoutScoreComparison.areComparable(
            ScoredAttempt(MetricId("test"), completed),
            ScoredAttempt(MetricId("test"), completed.copy(elapsed = Duration.ofMinutes(9))),
        ))
        assertFalse(WorkoutScoreComparison.areComparable(ScoredAttempt(MetricId("test"), completed), ScoredAttempt(MetricId("test"), capped)))
        assertFalse(WorkoutScoreComparison.areComparable(ScoredAttempt(MetricId("test"), capped), ScoredAttempt(MetricId("test"), capped)))
        assertFalse(WorkoutScoreComparison.areComparable(ScoredAttempt(MetricId("test"), capped), ScoredAttempt(MetricId("test"), dnf)))
        val best = ScoredAttempt(MetricId("test"), completed)
        val faster = ScoredAttempt(MetricId("test"), completed.copy(elapsed = Duration.ofMinutes(7)))
        assertEquals(
            faster,
            WorkoutScoreComparison.bestOf(listOf(ScoredAttempt(MetricId("test"), dnf), best, ScoredAttempt(MetricId("test"), capped), faster)),
        )
        assertNull(WorkoutScoreComparison.bestOf(listOf(ScoredAttempt(MetricId("test"), dnf), ScoredAttempt(MetricId("test"), capped))))
        assertNull(WorkoutScoreComparison.bestOf(emptyList()))
    }
}
