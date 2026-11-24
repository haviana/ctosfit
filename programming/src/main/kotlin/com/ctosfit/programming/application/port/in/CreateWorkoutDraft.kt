package com.ctosfit.programming.application.port.`in`

import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutDraft

fun interface CreateWorkoutDraft {
    fun create(title: String, initialBlocks: List<WorkoutBlock> = emptyList()): WorkoutDraft
}
