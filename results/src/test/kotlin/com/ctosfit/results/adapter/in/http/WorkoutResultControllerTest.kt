package com.ctosfit.results.adapter.`in`.http

import com.ctosfit.results.application.port.`in`.GetMyWorkoutResults
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResult
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResultCommand
import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId
import com.ctosfit.results.domain.CaloriesScore
import com.ctosfit.results.domain.CompletionScore
import com.ctosfit.results.domain.CompletionStatus
import com.ctosfit.results.domain.DistanceScore
import com.ctosfit.results.domain.DistanceUnit
import com.ctosfit.results.domain.LoadScore
import com.ctosfit.results.domain.LoadUnit
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId
import com.ctosfit.results.domain.RepsScore
import com.ctosfit.results.domain.ResultId
import com.ctosfit.results.domain.RoundsAndRepsScore
import com.ctosfit.results.domain.TimeOutcome
import com.ctosfit.results.domain.TimeScore
import com.ctosfit.results.domain.WorkoutResult
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutResultControllerTest {
    @Test
    fun `typed score request creates each explicit domain score`() {
        assertEquals(
            LoadScore(BigDecimal("42.5"), LoadUnit.KILOGRAM),
            WorkoutScoreRequest("LOAD", amount = BigDecimal("42.5"), unit = "KILOGRAM").toDomain(),
        )
        assertEquals(
            TimeScore(Duration.ofSeconds(518), TimeOutcome.COMPLETED),
            WorkoutScoreRequest("TIME", elapsedSeconds = 518, timeOutcome = "COMPLETED").toDomain(),
        )
        assertEquals(RepsScore(96), WorkoutScoreRequest("REPS", reps = 96).toDomain())
        assertEquals(
            RoundsAndRepsScore(5, 8),
            WorkoutScoreRequest("ROUNDS_AND_REPS", rounds = 5, additionalReps = 8).toDomain(),
        )
        assertEquals(
            DistanceScore(BigDecimal("1500"), DistanceUnit.METER),
            WorkoutScoreRequest("DISTANCE", amount = BigDecimal("1500"), unit = "METER").toDomain(),
        )
        assertEquals(CaloriesScore(80), WorkoutScoreRequest("CALORIES", calories = 80).toDomain())
        assertEquals(
            CompletionScore(CompletionStatus.NOT_COMPLETED),
            WorkoutScoreRequest("COMPLETION", completionStatus = "NOT_COMPLETED").toDomain(),
        )
    }

    @Test
    fun `request rejects missing extra and unknown score fields`() {
        assertFailsWith<IllegalArgumentException> {
            WorkoutScoreRequest("LOAD", amount = BigDecimal("40")).toDomain()
        }
        assertFailsWith<IllegalArgumentException> {
            WorkoutScoreRequest("REPS", reps = 10, amount = BigDecimal("40")).toDomain()
        }
        assertFailsWith<IllegalArgumentException> {
            WorkoutScoreRequest("UNSTRUCTURED", reps = 10).toDomain()
        }
    }

    @Test
    fun `controller returns the submitted score and only the athlete history`() {
        val submit = RecordingSubmitUseCase()
        val history = RecordingHistoryUseCase(listOf(result()))
        val controller = WorkoutResultController(submit, history)
        val request = SubmitWorkoutResultRequest(
            publicationId = "publication-1",
            metricId = "back-squat",
            prescriptionId = "RX",
            score = WorkoutScoreRequest("LOAD", amount = BigDecimal("60"), unit = "KILOGRAM"),
        )

        val submitted = controller.submit(request)
        val historyResponse = controller.history()

        assertEquals(201, submitted.statusCode.value())
        assertEquals("publication-1", submit.command?.publicationId?.value)
        assertEquals(LoadScore(BigDecimal("60"), LoadUnit.KILOGRAM), submit.command?.score)
        assertEquals(listOf("result-1"), historyResponse.map { it.id })
        assertEquals("COMPLETED", historyResponse.single().score.timeOutcome)
    }

    private fun result() = WorkoutResult(
        id = ResultId("result-1"),
        boxId = BoxId("box-1"),
        athleteId = AthleteId("athlete-1"),
        publicationId = PublicationId("publication-1"),
        metricId = MetricId("back-squat"),
        prescriptionId = PrescriptionId("RX"),
        submittedAt = Instant.parse("2026-11-24T17:00:00Z"),
        score = TimeScore(Duration.ofSeconds(518), TimeOutcome.COMPLETED),
    )

    private inner class RecordingSubmitUseCase : SubmitWorkoutResult {
        var command: SubmitWorkoutResultCommand? = null

        override fun submit(command: SubmitWorkoutResultCommand): WorkoutResult {
            this.command = command
            return result().copy(score = command.score)
        }
    }

    private class RecordingHistoryUseCase(
        private val results: List<WorkoutResult>,
    ) : GetMyWorkoutResults {
        override fun get(): List<WorkoutResult> = results
    }
}
