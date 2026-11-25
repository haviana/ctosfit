package com.ctosfit.programming.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutFormatTest {
    @Test
    fun `format keeps its timing and round parameters as structured values`() {
        assertEquals(720, WorkoutFormat.Amrap(720).durationSeconds)
        assertEquals(10, WorkoutFormat.Emom(60, 10).intervalCount)
        assertEquals(600, WorkoutFormat.ForTime(600).timeCapSeconds)
        assertEquals(8, WorkoutFormat.Tabata(20, 10, 8).rounds)
        assertEquals(5, WorkoutFormat.Intervals(5, 180, 60).rounds)
        assertEquals(5, WorkoutFormat.RoundsAndReps(5).rounds)
        assertEquals(5, WorkoutFormat.SetsAndReps(5, 120).sets)
    }

    @Test
    fun `format parameters must be positive`() {
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.Amrap(0) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.Emom(60, 0) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.ForTime(0) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.Tabata(20, 0, 8) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.Intervals(0, 180, 60) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.RoundsAndReps(0) }
        assertFailsWith<IllegalArgumentException> { WorkoutFormat.SetsAndReps(5, 0) }
    }
}
