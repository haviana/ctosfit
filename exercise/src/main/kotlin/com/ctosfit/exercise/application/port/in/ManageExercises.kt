package com.ctosfit.exercise.application.port.`in`

import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseCategory
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.ExerciseName
import java.time.Instant

data class SaveExerciseCommand(
    val name: ExerciseName,
    val category: ExerciseCategory,
    val equipment: Set<String> = emptySet(),
    val instructions: String? = null,
    val mediaReference: String? = null,
)

interface ManageExercises {
    fun create(command: SaveExerciseCommand): Exercise

    fun update(exerciseId: ExerciseId, command: SaveExerciseCommand): Exercise

    fun archive(exerciseId: ExerciseId, at: Instant): Exercise

    fun search(query: String, includeArchived: Boolean = false): List<Exercise>
}
