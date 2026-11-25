package com.ctosfit.exercise.adapter.`in`.http

import com.ctosfit.exercise.application.port.`in`.ManageExercises
import com.ctosfit.exercise.application.port.`in`.SaveExerciseCommand
import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseCategory
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.ExerciseName
import com.ctosfit.exercise.domain.TenantId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseControllerTest {
    @Test
    fun `create translates request to command and returns created exercise`() {
        val useCase = RecordingManageExercises()
        val controller = ExerciseController(useCase)

        val response = controller.create(
            SaveExerciseRequest(
                name = "  Back squat  ",
                category = "Strength",
                equipment = setOf("Barbell", "Rack"),
                instructions = "Keep the brace.",
                mediaReference = null,
            ),
        )

        assertEquals(201, response.statusCode.value())
        assertEquals(ExerciseName("  Back squat  "), useCase.created?.name)
        assertEquals(setOf("Barbell", "Rack"), useCase.created?.equipment)
        assertEquals("exercise-1", response.body?.id)
    }

    @Test
    fun `search returns application results and preserves archive filter`() {
        val useCase = RecordingManageExercises()
        val controller = ExerciseController(useCase)

        val response = controller.search(query = "squat", includeArchived = true)

        assertEquals("squat", useCase.searchedQuery)
        assertEquals(true, useCase.includeArchived)
        assertEquals("exercise-1", response.single().id)
    }

    private class RecordingManageExercises : ManageExercises {
        var created: SaveExerciseCommand? = null
        var searchedQuery: String? = null
        var includeArchived: Boolean? = null
        private val exercise = Exercise.create(
            id = ExerciseId("exercise-1"),
            tenantId = TenantId("box-1"),
            name = ExerciseName("Back squat"),
            category = ExerciseCategory("Strength"),
        )

        override fun create(command: SaveExerciseCommand): Exercise {
            created = command
            return exercise
        }

        override fun update(exerciseId: ExerciseId, command: SaveExerciseCommand): Exercise = exercise

        override fun archive(exerciseId: ExerciseId, at: Instant): Exercise = exercise

        override fun search(query: String, includeArchived: Boolean): List<Exercise> {
            searchedQuery = query
            this.includeArchived = includeArchived
            return listOf(exercise)
        }
    }
}
