package com.ctosfit.programming.domain

import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutDraftTest {
    private val coachId = CoachId("coach-1")
    private val boxId = BoxId("box-1")
    private val exerciseId = ExerciseId("exercise-1")

    @Test
    fun `draft preserves block ordering and advances version after edits`() {
        val warmUp = block("warm-up", "Warm up", BlockIntent.WARM_UP)
        val workout = block("metcon", "Metcon", BlockIntent.CONDITIONING)
        val draft = WorkoutDraft(WorkoutId("wod-1"), boxId, "Monday", listOf(warmUp, workout))

        draft.moveBlock(workout.id, 0)
        draft.rename("Monday WOD")

        assertEquals(listOf(workout.id, warmUp.id), draft.blocks.map(WorkoutBlock::id))
        assertEquals("Monday WOD", draft.title)
        assertEquals(3, draft.version)
    }

    @Test
    fun `publishing captures a detached versioned snapshot`() {
        val mutableMovements = mutableListOf(movement("Push-ups"))
        val metcon = WorkoutBlock(
            id = WorkoutBlockId("metcon"),
            title = "Metcon",
            intent = BlockIntent.CONDITIONING,
            format = WorkoutFormat.Amrap(durationSeconds = 12 * 60),
            movements = mutableMovements,
        )
        val initialBlocks = mutableListOf(metcon)
        val draft = WorkoutDraft(WorkoutId("wod-1"), boxId, "Monday", initialBlocks)

        val publication = draft.publish(
            publicationId = PublicationId("publication-1"),
            scheduledFor = LocalDate.parse("2026-11-25"),
            publishedAt = Instant.parse("2026-11-24T12:00:00Z"),
            publishedBy = coachId,
        )

        initialBlocks.clear()
        mutableMovements.clear()
        draft.rename("Changed draft")
        draft.replaceBlock(
            metcon.id,
            block("metcon", "Changed block", BlockIntent.STRENGTH),
        )

        assertEquals(1, publication.version)
        assertEquals(boxId, publication.boxId)
        assertEquals("Monday", publication.title)
        assertEquals("Metcon", publication.blocks.single().title)
        assertEquals("Push-ups", publication.blocks.single().movements.single().movementName)
        assertEquals(3, draft.version)
    }

    @Test
    fun `publication collections cannot be mutated by callers`() {
        val draft = WorkoutDraft(WorkoutId("wod-1"), boxId, "Monday", listOf(block("metcon", "Metcon")))
        val publication = draft.publish(
            PublicationId("publication-1"),
            LocalDate.parse("2026-11-25"),
            Instant.parse("2026-11-24T12:00:00Z"),
            coachId,
        )

        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (publication.blocks as MutableList<WorkoutBlock>).clear()
        }
        assertFailsWith<UnsupportedOperationException> {
            @Suppress("UNCHECKED_CAST")
            (publication.blocks.single().movements as MutableList<MovementPrescription>).clear()
        }
    }

    @Test
    fun `empty draft cannot be published`() {
        val draft = WorkoutDraft(WorkoutId("wod-1"), boxId, "Monday")

        assertFailsWith<IllegalStateException> {
            draft.publish(
                PublicationId("publication-1"),
                LocalDate.parse("2026-11-25"),
                Instant.parse("2026-11-24T12:00:00Z"),
                coachId,
            )
        }
    }

    @Test
    fun `athlete can select an available prescription variant from the published snapshot`() {
        val draft = WorkoutDraft(
            WorkoutId("wod-1"),
            boxId,
            "Monday",
            listOf(block("metcon", "Metcon")),
        )

        val publication = draft.publish(
            PublicationId("publication-1"),
            LocalDate.parse("2026-11-25"),
            Instant.parse("2026-11-24T12:00:00Z"),
            coachId,
        )
        val movement = publication.blocks.single().movements.single()

        assertEquals(listOf(MovementTarget.Repetitions(10)), movement.targetsFor(PrescriptionVariant.RX))
        assertEquals(listOf(MovementTarget.Repetitions(8)), movement.targetsFor(PrescriptionVariant.SCALED))
        assertEquals(listOf(MovementTarget.Repetitions(6)), movement.targetsFor(PrescriptionVariant.BEGINNER))
    }

    private fun block(
        id: String,
        title: String,
        intent: BlockIntent = BlockIntent.CONDITIONING,
    ) = WorkoutBlock(
        id = WorkoutBlockId(id),
        title = title,
        intent = intent,
        format = WorkoutFormat.ForTime(),
        movements = listOf(movement("Burpees")),
    )

    private fun movement(name: String) =
        MovementPrescription(
            exerciseId = exerciseId,
            movementName = name,
            variants = mapOf(
                PrescriptionVariant.RX to listOf(MovementTarget.Repetitions(10)),
                PrescriptionVariant.SCALED to listOf(MovementTarget.Repetitions(8)),
                PrescriptionVariant.BEGINNER to listOf(MovementTarget.Repetitions(6)),
            ),
        )
}
