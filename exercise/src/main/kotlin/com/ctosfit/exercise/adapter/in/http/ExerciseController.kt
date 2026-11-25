package com.ctosfit.exercise.adapter.`in`.http

import com.ctosfit.exercise.application.port.`in`.ManageExercises
import com.ctosfit.exercise.application.port.`in`.SaveExerciseCommand
import com.ctosfit.exercise.application.service.ExerciseNotFoundException
import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseCategory
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.ExerciseName
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

data class SaveExerciseRequest(
    val name: String,
    val category: String,
    val equipment: Set<String> = emptySet(),
    val instructions: String? = null,
    val mediaReference: String? = null,
) {
    fun toCommand(): SaveExerciseCommand = SaveExerciseCommand(
        name = ExerciseName(name),
        category = ExerciseCategory(category),
        equipment = equipment,
        instructions = instructions,
        mediaReference = mediaReference,
    )
}

data class ExerciseResponse(
    val id: String,
    val name: String,
    val category: String,
    val equipment: Set<String>,
    val instructions: String?,
    val mediaReference: String?,
    val archivedAt: Instant?,
) {
    companion object {
        fun from(exercise: Exercise) = ExerciseResponse(
            id = exercise.id.value,
            name = exercise.name.value,
            category = exercise.category.value,
            equipment = exercise.equipment,
            instructions = exercise.instructions,
            mediaReference = exercise.mediaReference,
            archivedAt = exercise.archivedAt,
        )
    }
}

@RestController
@RequestMapping("/api/coaches/exercises")
class ExerciseController(
    private val manageExercises: ManageExercises,
) {
    @PostMapping
    fun create(@RequestBody request: SaveExerciseRequest): ResponseEntity<ExerciseResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ExerciseResponse.from(manageExercises.create(request.toCommand())))

    @GetMapping
    fun search(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "false") includeArchived: Boolean,
    ): List<ExerciseResponse> =
        manageExercises.search(query, includeArchived).map(ExerciseResponse::from)

    @PutMapping("/{exerciseId}")
    fun update(
        @PathVariable exerciseId: String,
        @RequestBody request: SaveExerciseRequest,
    ): ExerciseResponse =
        ExerciseResponse.from(manageExercises.update(ExerciseId(exerciseId), request.toCommand()))

    @PostMapping("/{exerciseId}/archive")
    fun archive(@PathVariable exerciseId: String): ExerciseResponse =
        ExerciseResponse.from(manageExercises.archive(ExerciseId(exerciseId), Instant.now()))
}

@RestControllerAdvice(assignableTypes = [ExerciseController::class])
class ExerciseHttpExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (exception.message ?: "Invalid exercise request")))

    @ExceptionHandler(IllegalStateException::class)
    fun conflict(exception: IllegalStateException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("error" to (exception.message ?: "Exercise cannot be changed")))

    @ExceptionHandler(ExerciseNotFoundException::class)
    fun notFound(exception: ExerciseNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to exception.message.orEmpty()))
}
