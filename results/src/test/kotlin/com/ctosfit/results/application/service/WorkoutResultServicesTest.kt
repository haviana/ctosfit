package com.ctosfit.results.application.service

import com.ctosfit.results.application.port.`in`.SubmitWorkoutResultCommand
import com.ctosfit.results.application.port.out.AthleteContext
import com.ctosfit.results.application.port.out.CurrentCoachBoxProvider
import com.ctosfit.results.application.port.out.CurrentAthleteContextProvider
import com.ctosfit.results.application.port.out.PublishedWorkoutDetails
import com.ctosfit.results.application.port.out.PublishedWorkoutLookup
import com.ctosfit.results.application.port.out.WorkoutResultRepository
import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId
import com.ctosfit.results.domain.LoadScore
import com.ctosfit.results.domain.LoadUnit
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId
import com.ctosfit.results.domain.ResultId
import com.ctosfit.results.domain.WorkoutResult
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutResultServicesTest {
    private val boxId = BoxId("box-1")
    private val athleteId = AthleteId("athlete-1")
    private val publicationId = PublicationId("publication-1")
    private val metricId = MetricId("back-squat")
    private val prescriptionId = PrescriptionId("rx")
    private val fixedTime = Instant.parse("2026-11-24T17:00:00Z")
    private var lastPublicationLookup: Pair<BoxId, PublicationId>? = null

    @Test
    fun `submit takes box and athlete from trusted context and validates published workout`() {
        val repository = RecordingRepository()
        val service = SubmitWorkoutResultService(
            athleteContextProvider = CurrentAthleteContextProvider { AthleteContext(boxId, athleteId) },
            publishedWorkoutLookup = publishedWorkoutLookup(),
            workoutResultRepository = repository,
            clock = Clock.fixed(fixedTime, ZoneOffset.UTC),
            newResultId = { ResultId("result-1") },
        )

        val saved = service.submit(command())

        assertEquals(boxId, saved.boxId)
        assertEquals(athleteId, saved.athleteId)
        assertEquals(publicationId, saved.publicationId)
        assertEquals(metricId, saved.metricId)
        assertEquals(prescriptionId, saved.prescriptionId)
        assertEquals(fixedTime, saved.submittedAt)
        assertEquals(saved, repository.saved.single())
        assertEquals(boxId to publicationId, lastPublicationLookup)
    }

    @Test
    fun `submission hides publication outside current box`() {
        var repositorySaved = false
        val repository = object : WorkoutResultRepository by RecordingRepository() {
            override fun save(result: WorkoutResult): WorkoutResult {
                repositorySaved = true
                return result
            }
        }
        val lookup = PublishedWorkoutLookup { _, _ -> details(boxId = BoxId("another-box")) }
        val service = SubmitWorkoutResultService(
            CurrentAthleteContextProvider { AthleteContext(boxId, athleteId) }, lookup, repository,
        )

        assertFailsWith<PublishedWorkoutNotFoundException> { service.submit(command()) }
        assertEquals(false, repositorySaved)
    }

    @Test
    fun `submission rejects metric or prescription not in immutable publication`() {
        val service = SubmitWorkoutResultService(
            CurrentAthleteContextProvider { AthleteContext(boxId, athleteId) },
            publishedWorkoutLookup(),
            RecordingRepository(),
        )

        assertFailsWith<InvalidWorkoutResultException> {
            service.submit(command(metricId = MetricId("unrelated-metric")))
        }
        assertFailsWith<InvalidWorkoutResultException> {
            service.submit(command(prescriptionId = PrescriptionId("unpublished-variant")))
        }
    }

    @Test
    fun `history query uses only trusted box and athlete scope`() {
        val repository = RecordingRepository()
        val service = GetMyWorkoutResultsService(
            CurrentAthleteContextProvider { AthleteContext(boxId, athleteId) },
            repository,
        )

        assertEquals(emptyList(), service.get())
        assertEquals(boxId to athleteId, repository.lastHistoryQuery)
    }

    @Test
    fun `coach results query uses the trusted box scope`() {
        val repository = RecordingRepository()
        val service = GetBoxWorkoutResultsService(CurrentCoachBoxProvider { boxId }, repository)

        assertEquals(emptyList(), service.get())
        assertEquals(boxId, repository.lastBoxResultsQuery)
    }

    @Test
    fun `personal bests compare only matching metric prescription and score units`() {
        val repository = RecordingRepository()
        repository.saved += result("rx-80", metricId, prescriptionId, LoadScore(BigDecimal("80"), LoadUnit.KILOGRAM))
        repository.saved += result("rx-100", metricId, prescriptionId, LoadScore(BigDecimal("100"), LoadUnit.KILOGRAM))
        repository.saved += result(
            "scaled-110", metricId, PrescriptionId("scaled"), LoadScore(BigDecimal("110"), LoadUnit.KILOGRAM),
        )
        repository.saved += result(
            "other-90", MetricId("deadlift"), prescriptionId, LoadScore(BigDecimal("90"), LoadUnit.KILOGRAM),
        )
        val service = GetMyPersonalBestsService(
            CurrentAthleteContextProvider { AthleteContext(boxId, athleteId) },
            repository,
        )

        val bests = service.get()

        assertEquals(setOf("rx-100", "scaled-110", "other-90"), bests.map { it.resultId.value }.toSet())
    }

    private fun command(
        metricId: MetricId = this.metricId,
        prescriptionId: PrescriptionId = this.prescriptionId,
    ) = SubmitWorkoutResultCommand(
        publicationId = publicationId,
        metricId = metricId,
        prescriptionId = prescriptionId,
        score = LoadScore(BigDecimal("100"), LoadUnit.KILOGRAM),
    )

    private fun publishedWorkoutLookup() = PublishedWorkoutLookup { requestedBox, requestedPublication ->
        lastPublicationLookup = requestedBox to requestedPublication
        if (requestedBox == boxId && requestedPublication == publicationId) details() else null
    }

    private fun details(
        boxId: BoxId = this.boxId,
        publicationId: PublicationId = this.publicationId,
    ) = PublishedWorkoutDetails(boxId, publicationId, setOf(metricId), setOf(prescriptionId))

    private fun result(
        id: String,
        metricId: MetricId,
        prescriptionId: PrescriptionId,
        score: LoadScore,
    ) = WorkoutResult(
        id = ResultId(id),
        boxId = boxId,
        athleteId = athleteId,
        publicationId = publicationId,
        metricId = metricId,
        prescriptionId = prescriptionId,
        submittedAt = fixedTime,
        score = score,
    )

    private class RecordingRepository : WorkoutResultRepository {
        val saved = mutableListOf<WorkoutResult>()
        var lastHistoryQuery: Pair<BoxId, AthleteId>? = null
        var lastBoxResultsQuery: BoxId? = null

        override fun save(result: WorkoutResult): WorkoutResult {
            saved += result
            return result
        }

        override fun findByBoxAndAthlete(boxId: BoxId, athleteId: AthleteId): List<WorkoutResult> {
            lastHistoryQuery = boxId to athleteId
            return saved.filter { it.boxId == boxId && it.athleteId == athleteId }
        }

        override fun findByBox(boxId: BoxId): List<WorkoutResult> {
            lastBoxResultsQuery = boxId
            return saved.filter { it.boxId == boxId }
        }
    }
}
