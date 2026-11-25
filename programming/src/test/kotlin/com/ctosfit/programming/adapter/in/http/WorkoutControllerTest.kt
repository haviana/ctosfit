package com.ctosfit.programming.adapter.`in`.http

import com.ctosfit.programming.application.port.`in`.CreateWorkoutDraft
import com.ctosfit.programming.application.port.`in`.EditWorkoutDraft
import com.ctosfit.programming.application.port.`in`.GetPublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.`in`.GetScheduledWorkoutSnapshots
import com.ctosfit.programming.application.port.`in`.PublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.`in`.PublishWorkout
import com.ctosfit.programming.application.port.out.CurrentBoxContextProvider
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
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutControllerTest {
    private val boxId = BoxId("trusted-box")
    private val publication = WorkoutPublication(
        id = PublicationId("publication-1"),
        workoutId = WorkoutId("workout-1"),
        boxId = boxId,
        version = 2,
        title = "Friday WOD",
        scheduledFor = LocalDate.parse("2026-11-27"),
        publishedAt = Instant.parse("2026-11-24T09:00:00Z"),
        publishedBy = CoachId("coach-1"),
        blocks = listOf(block("block-1")),
    )
    private var lastReadBoxId: BoxId? = null
    private var lastScheduleQuery: Pair<BoxId, LocalDate>? = null

    @Test
    fun `create maps structured formats movements and variants into the use case`() {
        val controller = controller()
        val response = controller.create(
            CreateWorkoutRequest(
                title = "Friday WOD",
                blocks = listOf(
                    WorkoutBlockRequest(
                        title = "Conditioning",
                        intent = "CONDITIONING",
                        format = WorkoutFormatRequest("AMRAP", durationSeconds = 720),
                        movements = listOf(
                            MovementRequest(
                                exerciseId = "row",
                                movementName = "Row",
                                variants = mapOf(
                                    "RX" to listOf(MovementTargetRequest("CALORIES", "10")),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(201, response.statusCode.value())
        assertEquals("Friday WOD", response.body?.title)
        assertEquals("AMRAP", response.body?.blocks?.single()?.format?.kind)
        assertEquals("10", response.body?.blocks?.single()?.movements?.single()?.variants?.get("RX")?.single()?.value)
    }

    @Test
    fun `member publication read uses current box context instead of a request box`() {
        val response = controller().getPublication("publication-1")

        assertEquals(boxId, lastReadBoxId)
        assertEquals(publication.id.value, response.body?.id)
        assertEquals(publication.title, response.body?.title)
    }

    @Test
    fun `scheduled workout list uses verified current box and requested date`() {
        val date = LocalDate.parse("2026-11-27")

        val response = controller().getScheduledWorkouts(date)

        assertEquals(boxId to date, lastScheduleQuery)
        assertEquals(publication.id.value, response.single().id)
    }

    private fun controller(): WorkoutController = WorkoutController(
        createWorkoutDraft = CreateWorkoutDraft { title, blocks ->
            WorkoutDraft(WorkoutId("workout-1"), boxId, title, blocks)
        },
        editWorkoutDraft = object : EditWorkoutDraft {
            private val draft = WorkoutDraft(WorkoutId("workout-1"), boxId, "Friday WOD", listOf(block("block-1")))

            override fun rename(workoutId: WorkoutId, title: String): WorkoutDraft = draft
            override fun addBlock(workoutId: WorkoutId, block: WorkoutBlock, position: Int?): WorkoutDraft = draft
            override fun replaceBlock(workoutId: WorkoutId, blockId: WorkoutBlockId, replacement: WorkoutBlock): WorkoutDraft = draft
            override fun moveBlock(workoutId: WorkoutId, blockId: WorkoutBlockId, position: Int): WorkoutDraft = draft
            override fun removeBlock(workoutId: WorkoutId, blockId: WorkoutBlockId): WorkoutDraft = draft
        },
        publishWorkout = PublishWorkout { _, _ -> publication },
        getPublishedWorkoutSnapshot = GetPublishedWorkoutSnapshot { requestedBoxId, requestedPublicationId ->
            lastReadBoxId = requestedBoxId
            if (requestedBoxId == boxId && requestedPublicationId == publication.id) {
                PublishedWorkoutSnapshot(boxId, publication.id, setOf("row"), setOf("RX"), publication)
            } else {
                null
            }
        },
        getScheduledWorkoutSnapshots = GetScheduledWorkoutSnapshots { requestedBoxId, date ->
            lastScheduleQuery = requestedBoxId to date
            if (requestedBoxId == boxId && date == publication.scheduledFor) listOf(publication) else emptyList()
        },
        currentBoxContextProvider = CurrentBoxContextProvider { boxId },
    )

    private fun block(id: String) = WorkoutBlock(
        id = WorkoutBlockId(id),
        title = "Conditioning",
        intent = BlockIntent.CONDITIONING,
        format = null,
        movements = listOf(
            MovementPrescription(
                exerciseId = ExerciseId("row"),
                movementName = "Row",
                variants = mapOf(PrescriptionVariant.RX to listOf(MovementTarget.Calories(10))),
            ),
        ),
    )
}
