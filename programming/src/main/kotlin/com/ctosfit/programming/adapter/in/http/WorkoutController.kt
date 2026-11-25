package com.ctosfit.programming.adapter.`in`.http

import com.ctosfit.programming.application.port.`in`.CreateWorkoutDraft
import com.ctosfit.programming.application.port.`in`.EditWorkoutDraft
import com.ctosfit.programming.application.port.`in`.GetPublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.`in`.GetScheduledWorkoutSnapshots
import com.ctosfit.programming.application.port.`in`.PublishWorkout
import com.ctosfit.programming.application.port.out.CurrentBoxContextProvider
import com.ctosfit.programming.application.service.WorkoutDraftNotFoundException
import com.ctosfit.programming.domain.BlockIntent
import com.ctosfit.programming.domain.ExerciseId
import com.ctosfit.programming.domain.MovementPrescription
import com.ctosfit.programming.domain.MovementTarget
import com.ctosfit.programming.domain.MovementUnit
import com.ctosfit.programming.domain.PrescriptionVariant
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutFormat
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

data class CreateWorkoutRequest(
    val title: String,
    val blocks: List<WorkoutBlockRequest> = emptyList(),
)

data class RenameWorkoutRequest(val title: String)

data class AddWorkoutBlockRequest(
    val block: WorkoutBlockRequest,
    val position: Int? = null,
)

data class MoveWorkoutBlockRequest(val position: Int)

data class PublishWorkoutRequest(val scheduledFor: LocalDate)

data class WorkoutBlockRequest(
    val title: String,
    val intent: String,
    val format: WorkoutFormatRequest? = null,
    val movements: List<MovementRequest>,
    val instructions: String? = null,
)

data class WorkoutFormatRequest(
    val kind: String,
    val durationSeconds: Int? = null,
    val intervalSeconds: Int? = null,
    val intervalCount: Int? = null,
    val workSeconds: Int? = null,
    val restSeconds: Int? = null,
    val rounds: Int? = null,
    val sets: Int? = null,
    val timeCapSeconds: Int? = null,
) {
    fun toDomain(): WorkoutFormat = when (kind) {
        "AMRAP" -> WorkoutFormat.Amrap(requireField(durationSeconds, "durationSeconds"))
        "EMOM" -> WorkoutFormat.Emom(
            requireField(intervalSeconds, "intervalSeconds"),
            requireField(intervalCount, "intervalCount"),
        )
        "FOR_TIME" -> WorkoutFormat.ForTime(timeCapSeconds)
        "TABATA" -> WorkoutFormat.Tabata(
            requireField(workSeconds, "workSeconds"),
            requireField(restSeconds, "restSeconds"),
            requireField(rounds, "rounds"),
        )
        "INTERVALS" -> WorkoutFormat.Intervals(
            requireField(rounds, "rounds"),
            requireField(workSeconds, "workSeconds"),
            requireField(restSeconds, "restSeconds"),
        )
        "ROUNDS_AND_REPS" -> WorkoutFormat.RoundsAndReps(rounds)
        "SETS_AND_REPS" -> WorkoutFormat.SetsAndReps(requireField(sets, "sets"), restSeconds)
        "CHIPPER" -> WorkoutFormat.Chipper(timeCapSeconds)
        else -> throw IllegalArgumentException("Unsupported workout format: $kind")
    }

    private fun <T : Any> requireField(value: T?, name: String): T =
        requireNotNull(value) { "$name is required for $kind" }
}

data class MovementRequest(
    val exerciseId: String,
    val movementName: String,
    val variants: Map<String, List<MovementTargetRequest>>,
) {
    fun toDomain(): MovementPrescription = MovementPrescription(
        exerciseId = ExerciseId(exerciseId),
        movementName = movementName,
        variants = variants.mapKeys { (variant, _) -> enumValue<PrescriptionVariant>(variant) }
            .mapValues { (_, targets) -> targets.map(MovementTargetRequest::toDomain) },
    )
}

data class MovementTargetRequest(
    val kind: String,
    val value: String,
    val unit: String? = null,
) {
    fun toDomain(): MovementTarget = when (kind) {
        "REPETITIONS" -> MovementTarget.Repetitions(value.toInt())
        "LOAD" -> MovementTarget.Load(
            BigDecimal(value),
            enumValue<MovementUnit>(requireNotNull(unit) { "unit is required for LOAD" }),
        )
        "DISTANCE" -> MovementTarget.Distance(
            BigDecimal(value),
            enumValue<MovementUnit>(requireNotNull(unit) { "unit is required for DISTANCE" }),
        )
        "DURATION" -> MovementTarget.Duration(value.toInt())
        "CALORIES" -> MovementTarget.Calories(value.toInt())
        else -> throw IllegalArgumentException("Unsupported movement target: $kind")
    }
}

data class WorkoutBlockResponse(
    val id: String,
    val title: String,
    val intent: String,
    val format: WorkoutFormatRequest?,
    val movements: List<MovementResponse>,
    val instructions: String?,
)

data class MovementResponse(
    val exerciseId: String,
    val movementName: String,
    val variants: Map<String, List<MovementTargetResponse>>,
)

data class MovementTargetResponse(val kind: String, val value: String, val unit: String?)

data class WorkoutDraftResponse(
    val id: String,
    val title: String,
    val version: Int,
    val blocks: List<WorkoutBlockResponse>,
)

data class WorkoutPublicationResponse(
    val id: String,
    val workoutId: String,
    val version: Int,
    val title: String,
    val scheduledFor: LocalDate,
    val publishedAt: Instant,
    val publishedBy: String,
    val blocks: List<WorkoutBlockResponse>,
) {
    companion object {
        fun from(publication: WorkoutPublication) = WorkoutPublicationResponse(
            id = publication.id.value,
            workoutId = publication.workoutId.value,
            version = publication.version,
            title = publication.title,
            scheduledFor = publication.scheduledFor,
            publishedAt = publication.publishedAt,
            publishedBy = publication.publishedBy.value,
            blocks = publication.blocks.map(WorkoutBlock::toResponse),
        )
    }
}

@RestController
class WorkoutController(
    private val createWorkoutDraft: CreateWorkoutDraft,
    private val editWorkoutDraft: EditWorkoutDraft,
    private val publishWorkout: PublishWorkout,
    private val getPublishedWorkoutSnapshot: GetPublishedWorkoutSnapshot,
    private val getScheduledWorkoutSnapshots: GetScheduledWorkoutSnapshots,
    private val currentBoxContextProvider: CurrentBoxContextProvider,
) {
    @PostMapping("/api/coaches/workouts")
    fun create(@RequestBody request: CreateWorkoutRequest): ResponseEntity<WorkoutDraftResponse> {
        val draft = createWorkoutDraft.create(request.title, request.blocks.map { it.toDomain() })
        return ResponseEntity.status(HttpStatus.CREATED).body(draft.toResponse())
    }

    @PutMapping("/api/coaches/workouts/{workoutId}/title")
    fun rename(
        @PathVariable workoutId: String,
        @RequestBody request: RenameWorkoutRequest,
    ): WorkoutDraftResponse = editWorkoutDraft.rename(WorkoutId(workoutId), request.title).toResponse()

    @PostMapping("/api/coaches/workouts/{workoutId}/blocks")
    fun addBlock(
        @PathVariable workoutId: String,
        @RequestBody request: AddWorkoutBlockRequest,
    ): WorkoutDraftResponse {
        val block = request.block.toDomain(WorkoutBlockId(UUID.randomUUID().toString()))
        return editWorkoutDraft.addBlock(WorkoutId(workoutId), block, request.position).toResponse()
    }

    @PutMapping("/api/coaches/workouts/{workoutId}/blocks/{blockId}")
    fun replaceBlock(
        @PathVariable workoutId: String,
        @PathVariable blockId: String,
        @RequestBody request: WorkoutBlockRequest,
    ): WorkoutDraftResponse = editWorkoutDraft.replaceBlock(
        WorkoutId(workoutId),
        WorkoutBlockId(blockId),
        request.toDomain(WorkoutBlockId(blockId)),
    ).toResponse()

    @PatchMapping("/api/coaches/workouts/{workoutId}/blocks/{blockId}/position")
    fun moveBlock(
        @PathVariable workoutId: String,
        @PathVariable blockId: String,
        @RequestBody request: MoveWorkoutBlockRequest,
    ): WorkoutDraftResponse = editWorkoutDraft.moveBlock(
        WorkoutId(workoutId),
        WorkoutBlockId(blockId),
        request.position,
    ).toResponse()

    @DeleteMapping("/api/coaches/workouts/{workoutId}/blocks/{blockId}")
    fun removeBlock(
        @PathVariable workoutId: String,
        @PathVariable blockId: String,
    ): WorkoutDraftResponse = editWorkoutDraft.removeBlock(
        WorkoutId(workoutId),
        WorkoutBlockId(blockId),
    ).toResponse()

    @PostMapping("/api/coaches/workouts/{workoutId}/publish")
    fun publish(
        @PathVariable workoutId: String,
        @RequestBody request: PublishWorkoutRequest,
    ): WorkoutPublicationResponse = WorkoutPublicationResponse.from(
        publishWorkout.publish(WorkoutId(workoutId), request.scheduledFor),
    )

    @GetMapping("/api/athletes/workouts")
    fun getScheduledWorkouts(@RequestParam("date") date: LocalDate): List<WorkoutPublicationResponse> {
        val boxId = currentBoxContextProvider.currentBoxId()
        return getScheduledWorkoutSnapshots.get(boxId, date).map(WorkoutPublicationResponse::from)
    }

    @GetMapping("/api/athletes/publications/{publicationId}")
    fun getPublication(@PathVariable publicationId: String): ResponseEntity<WorkoutPublicationResponse> {
        val boxId = currentBoxContextProvider.currentBoxId()
        val snapshot = getPublishedWorkoutSnapshot.get(boxId, PublicationId(publicationId))
            ?: return ResponseEntity.notFound().build<WorkoutPublicationResponse>()
        return ResponseEntity.ok(WorkoutPublicationResponse.from(snapshot.snapshot))
    }
}

@RestControllerAdvice(assignableTypes = [WorkoutController::class])
class WorkoutHttpExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (exception.message ?: "Invalid workout request")))

    @ExceptionHandler(IllegalStateException::class)
    fun conflict(exception: IllegalStateException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("error" to (exception.message ?: "Workout cannot be changed")))

    @ExceptionHandler(WorkoutDraftNotFoundException::class)
    fun draftNotFound(exception: WorkoutDraftNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to exception.message.orEmpty()))
}

private fun WorkoutBlockRequest.toDomain(id: WorkoutBlockId = WorkoutBlockId(UUID.randomUUID().toString())) =
    WorkoutBlock(
        id = id,
        title = title,
        intent = enumValue(intent),
        format = format?.toDomain(),
        movements = movements.map(MovementRequest::toDomain),
        instructions = instructions,
    )

private fun WorkoutDraft.toResponse() = WorkoutDraftResponse(
    id = id.value,
    title = title,
    version = version,
    blocks = blocks.map(WorkoutBlock::toResponse),
)

private fun WorkoutBlock.toResponse() = WorkoutBlockResponse(
    id = id.value,
    title = title,
    intent = intent.name,
    format = format?.toRequest(),
    movements = movements.map { movement ->
        MovementResponse(
            exerciseId = movement.exerciseId.value,
            movementName = movement.movementName,
            variants = movement.variants.mapKeys { (variant, _) -> variant.name }
                .mapValues { (_, targets) -> targets.map(MovementTarget::toResponse) },
        )
    },
    instructions = instructions,
)

private fun WorkoutFormat.toRequest(): WorkoutFormatRequest = when (this) {
    is WorkoutFormat.Amrap -> WorkoutFormatRequest("AMRAP", durationSeconds = durationSeconds)
    is WorkoutFormat.Emom -> WorkoutFormatRequest(
        "EMOM", intervalSeconds = intervalSeconds, intervalCount = intervalCount,
    )
    is WorkoutFormat.ForTime -> WorkoutFormatRequest("FOR_TIME", timeCapSeconds = timeCapSeconds)
    is WorkoutFormat.Tabata -> WorkoutFormatRequest(
        "TABATA", workSeconds = workSeconds, restSeconds = restSeconds, rounds = rounds,
    )
    is WorkoutFormat.Intervals -> WorkoutFormatRequest(
        "INTERVALS", rounds = rounds, workSeconds = workSeconds, restSeconds = restSeconds,
    )
    is WorkoutFormat.RoundsAndReps -> WorkoutFormatRequest("ROUNDS_AND_REPS", rounds = rounds)
    is WorkoutFormat.SetsAndReps -> WorkoutFormatRequest("SETS_AND_REPS", restSeconds = restSeconds, sets = sets)
    is WorkoutFormat.Chipper -> WorkoutFormatRequest("CHIPPER", timeCapSeconds = timeCapSeconds)
}

private fun MovementTarget.toResponse(): MovementTargetResponse = when (this) {
    is MovementTarget.Repetitions -> MovementTargetResponse("REPETITIONS", count.toString(), null)
    is MovementTarget.Load -> MovementTargetResponse("LOAD", amount.toPlainString(), unit.name)
    is MovementTarget.Distance -> MovementTargetResponse("DISTANCE", amount.toPlainString(), unit.name)
    is MovementTarget.Duration -> MovementTargetResponse("DURATION", seconds.toString(), null)
    is MovementTarget.Calories -> MovementTargetResponse("CALORIES", count.toString(), null)
}

private inline fun <reified T : Enum<T>> enumValue(value: String): T =
    enumValues<T>().firstOrNull { it.name == value }
        ?: throw IllegalArgumentException("Unsupported ${T::class.simpleName}: $value")
