package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.domain.BlockIntent
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.CoachId
import com.ctosfit.programming.domain.ExerciseId
import com.ctosfit.programming.domain.MovementPrescription
import com.ctosfit.programming.domain.MovementTarget
import com.ctosfit.programming.domain.PrescriptionVariant
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GetPublishedWorkoutSnapshotServiceTest {
    @Test
    fun `query returns exact publication and only prescription variants available for every movement`() {
        val boxId = BoxId("box-1")
        val publicationId = PublicationId("publication-1")
        val publication = WorkoutPublication(
            id = publicationId,
            workoutId = WorkoutId("workout-1"),
            boxId = boxId,
            version = 2,
            title = "Training day",
            scheduledFor = LocalDate.parse("2026-11-24"),
            publishedAt = Instant.parse("2026-11-24T08:00:00Z"),
            publishedBy = CoachId("coach-1"),
            blocks = listOf(
                block("block-1", movement("back-squat", scaled = true)),
                block("block-2", movement("row", scaled = false)),
            ),
        )
        val repository = object : WorkoutPublicationRepository {
            override fun save(publication: WorkoutPublication): WorkoutPublication = publication

            override fun findByBoxAndId(
                requestedBox: BoxId,
                requestedPublication: PublicationId,
            ): WorkoutPublication? =
                if (requestedBox == boxId && requestedPublication == publicationId) publication else null

            override fun findByBoxAndScheduledDate(
                requestedBox: BoxId,
                scheduledFor: LocalDate,
            ): List<WorkoutPublication> =
                if (requestedBox == boxId && scheduledFor == publication.scheduledFor) listOf(publication) else emptyList()
        }
        val query = GetPublishedWorkoutSnapshotService(repository)

        val result = query.get(boxId, publicationId)

        assertEquals(boxId, result?.boxId)
        assertEquals(publicationId, result?.publicationId)
        assertEquals(setOf("back-squat", "row"), result?.metricIds)
        assertEquals(setOf("RX"), result?.prescriptionIds)
        assertSame(publication, result?.snapshot)
    }

    private fun movement(id: String, scaled: Boolean) = MovementPrescription(
        exerciseId = ExerciseId(id),
        movementName = id,
        variants = buildMap {
            put(PrescriptionVariant.RX, listOf(MovementTarget.Repetitions(10)))
            if (scaled) put(PrescriptionVariant.SCALED, listOf(MovementTarget.Repetitions(8)))
            put(PrescriptionVariant.BEGINNER, listOf(MovementTarget.Repetitions(5)))
        },
    )

    private fun block(id: String, movement: MovementPrescription) = WorkoutBlock(
        id = WorkoutBlockId(id),
        title = "Conditioning",
        intent = BlockIntent.CONDITIONING,
        format = null,
        movements = listOf(movement),
    )
}
