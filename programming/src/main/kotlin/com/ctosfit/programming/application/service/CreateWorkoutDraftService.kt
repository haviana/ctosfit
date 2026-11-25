package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.`in`.CreateWorkoutDraft
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId
import java.util.UUID

class CreateWorkoutDraftService(
    private val currentCoachContextProvider: CurrentCoachContextProvider,
    private val workoutDraftRepository: WorkoutDraftRepository,
) : CreateWorkoutDraft {
    override fun create(title: String, initialBlocks: List<WorkoutBlock>): WorkoutDraft {
        val context = currentCoachContextProvider.currentCoachContext()
        return workoutDraftRepository.save(
            WorkoutDraft(
                id = WorkoutId(UUID.randomUUID().toString()),
                boxId = context.boxId,
                title = title,
                initialBlocks = initialBlocks,
            ),
        )
    }
}
