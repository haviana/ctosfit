package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.out.CoachContext
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.domain.BlockIntent
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.CoachId
import com.ctosfit.programming.domain.ExerciseId
import com.ctosfit.programming.domain.MovementPrescription
import com.ctosfit.programming.domain.MovementTarget
import com.ctosfit.programming.domain.PrescriptionVariant
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkoutApplicationServiceTest {
    private val boxId = BoxId("box-1")
    private val coachId = CoachId("coach-1")

    @Test
    fun `draft edits resolve box and coach before tenant scoped lookup`() {
        val draft = draft()
        val drafts = InMemoryDraftRepository(draft)
        val service = EditWorkoutDraftService(
            drafts,
            CurrentCoachContextProvider { CoachContext(boxId, coachId) },
        )

        service.rename(draft.id, "Updated WOD")

        assertEquals(boxId, drafts.lastBoxLookup)
        assertEquals("Updated WOD", draft.title)
        assertEquals(2, draft.version)
    }

    @Test
    fun `unknown draft outside current box is indistinguishable from missing`() {
        val draft = draft()
        val drafts = InMemoryDraftRepository(draft)
        val service = EditWorkoutDraftService(
            drafts,
            CurrentCoachContextProvider { CoachContext(BoxId("another-box"), coachId) },
        )

        assertFailsWith<WorkoutDraftNotFoundException> { service.rename(draft.id, "Stolen") }
        assertEquals("Monday WOD", draft.title)
    }

    @Test
    fun `publishing stores immutable snapshot attributed to trusted coach`() {
        val draft = draft()
        draft.addBlock(block("block-1"))
        val drafts = InMemoryDraftRepository(draft)
        val publications = InMemoryPublicationRepository()
        val instant = Instant.parse("2026-11-24T09:00:00Z")
        val service = PublishWorkoutService(
            draftRepository = drafts,
            publicationRepository = publications,
            currentCoachContextProvider = CurrentCoachContextProvider { CoachContext(boxId, coachId) },
            clock = Clock.fixed(instant, ZoneOffset.UTC),
        )

        val published = service.publish(draft.id, LocalDate.parse("2026-11-24"))
        draft.rename("Edited after publication")

        assertEquals(boxId, drafts.lastBoxLookup)
        assertEquals(coachId, published.publishedBy)
        assertEquals(instant, published.publishedAt)
        assertEquals("Monday WOD", published.title)
        assertEquals("Monday WOD", publications.saved.single().title)
        assertEquals("Edited after publication", draft.title)
    }

    private fun draft() = WorkoutDraft(WorkoutId("workout-1"), boxId, "Monday WOD")

    private fun block(id: String) = WorkoutBlock(
        id = WorkoutBlockId(id),
        title = "Strength",
        intent = BlockIntent.STRENGTH,
        format = null,
        movements = listOf(
            MovementPrescription(
                exerciseId = ExerciseId("back-squat"),
                movementName = "Back squat",
                variants = mapOf(PrescriptionVariant.RX to listOf(MovementTarget.Repetitions(5))),
            ),
        ),
    )

    private class InMemoryDraftRepository(var draft: WorkoutDraft) : WorkoutDraftRepository {
        var lastBoxLookup: BoxId? = null

        override fun findByBoxAndId(boxId: BoxId, workoutId: WorkoutId): WorkoutDraft? {
            lastBoxLookup = boxId
            return draft.takeIf { it.boxId == boxId && it.id == workoutId }
        }

        override fun save(draft: WorkoutDraft): WorkoutDraft = draft.also { this.draft = it }
    }

    private class InMemoryPublicationRepository : WorkoutPublicationRepository {
        val saved = mutableListOf<WorkoutPublication>()

        override fun save(publication: WorkoutPublication): WorkoutPublication = publication.also(saved::add)

        override fun findByBoxAndId(
            boxId: BoxId,
            publicationId: com.ctosfit.programming.domain.PublicationId,
        ): WorkoutPublication? = saved.firstOrNull { it.boxId == boxId && it.id == publicationId }

        override fun findByBoxAndScheduledDate(
            boxId: BoxId,
            scheduledFor: LocalDate,
        ): List<WorkoutPublication> = saved.filter { it.boxId == boxId && it.scheduledFor == scheduledFor }
    }
}
