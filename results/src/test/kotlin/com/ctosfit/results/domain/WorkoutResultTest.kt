package com.ctosfit.results.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutResultTest {
    @Test
    fun `result keeps tenant athlete publication prescription and typed score together`() {
        val submittedAt = Instant.parse("2026-11-24T17:00:00Z")
        val result = WorkoutResult(
            id = ResultId("result-1"),
            boxId = BoxId("box-1"),
            athleteId = AthleteId("athlete-1"),
            publicationId = PublicationId("publication-v3"),
            metricId = MetricId("benchmark-fran-v1"),
            prescriptionId = PrescriptionId("scaled"),
            submittedAt = submittedAt,
            score = RoundsAndRepsScore(rounds = 8, additionalReps = 3),
        )

        assertEquals("box-1", result.boxId.value)
        assertEquals("athlete-1", result.athleteId.value)
        assertEquals("publication-v3", result.publicationId.value)
        assertEquals("benchmark-fran-v1", result.metricId.value)
        assertEquals("scaled", result.prescriptionId.value)
        assertEquals(submittedAt, result.submittedAt)
        assertEquals(RoundsAndRepsScore(8, 3), result.score)
    }
}
