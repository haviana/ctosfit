package com.ctosfit.programming.application.port.`in`

import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId

/** Mutates only a draft owned by the caller's trusted current box context. */
interface EditWorkoutDraft {
    fun rename(workoutId: WorkoutId, title: String): WorkoutDraft

    fun addBlock(workoutId: WorkoutId, block: WorkoutBlock, position: Int? = null): WorkoutDraft

    fun replaceBlock(workoutId: WorkoutId, blockId: WorkoutBlockId, replacement: WorkoutBlock): WorkoutDraft

    fun moveBlock(workoutId: WorkoutId, blockId: WorkoutBlockId, position: Int): WorkoutDraft

    fun removeBlock(workoutId: WorkoutId, blockId: WorkoutBlockId): WorkoutDraft
}
