package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.`in`.EditWorkoutDraft
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId

class WorkoutDraftNotFoundException : RuntimeException("Workout draft was not found")

class EditWorkoutDraftService(
    private val draftRepository: WorkoutDraftRepository,
    private val currentCoachContextProvider: CurrentCoachContextProvider,
) : EditWorkoutDraft {
    override fun rename(workoutId: WorkoutId, title: String): WorkoutDraft = mutate(workoutId) { it.rename(title) }

    override fun addBlock(workoutId: WorkoutId, block: WorkoutBlock, position: Int?): WorkoutDraft = mutate(workoutId) {
        if (position == null) it.addBlock(block) else it.addBlock(block, position)
    }

    override fun replaceBlock(
        workoutId: WorkoutId,
        blockId: WorkoutBlockId,
        replacement: WorkoutBlock,
    ): WorkoutDraft = mutate(workoutId) { it.replaceBlock(blockId, replacement) }

    override fun moveBlock(workoutId: WorkoutId, blockId: WorkoutBlockId, position: Int): WorkoutDraft =
        mutate(workoutId) { it.moveBlock(blockId, position) }

    override fun removeBlock(workoutId: WorkoutId, blockId: WorkoutBlockId): WorkoutDraft =
        mutate(workoutId) { it.removeBlock(blockId) }

    private inline fun mutate(workoutId: WorkoutId, change: (WorkoutDraft) -> Unit): WorkoutDraft {
        val context = currentCoachContextProvider.currentCoachContext()
        val draft = draftRepository.findByBoxAndId(context.boxId, workoutId) ?: throw WorkoutDraftNotFoundException()
        change(draft)
        return draftRepository.save(draft)
    }
}
