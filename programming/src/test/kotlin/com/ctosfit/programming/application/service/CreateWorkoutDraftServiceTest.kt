package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.out.CoachContext
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.CoachId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateWorkoutDraftServiceTest {
    @Test
    fun `workout draft tenant comes from trusted context provider`() {
        var saved: WorkoutDraft? = null
        val service = CreateWorkoutDraftService(
            CurrentCoachContextProvider {
                CoachContext(BoxId("verified-box"), CoachId("verified-coach"))
            },
            object : WorkoutDraftRepository {
                override fun findByBoxAndId(boxId: BoxId, workoutId: WorkoutId): WorkoutDraft? = null

                override fun save(draft: WorkoutDraft): WorkoutDraft = draft.also { saved = it }
            },
        )

        val draft = service.create("Monday WOD")

        assertEquals(BoxId("verified-box"), draft.boxId)
        assertEquals(draft, saved)
    }
}
