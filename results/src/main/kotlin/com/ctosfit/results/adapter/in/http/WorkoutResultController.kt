package com.ctosfit.results.adapter.`in`.http

import com.ctosfit.results.application.port.`in`.GetMyWorkoutResults
import com.ctosfit.results.application.port.`in`.GetBoxWorkoutResults
import com.ctosfit.results.application.port.`in`.GetMyPersonalBests
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResult
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResultCommand
import com.ctosfit.results.application.service.InvalidWorkoutResultException
import com.ctosfit.results.application.service.PublishedWorkoutNotFoundException
import com.ctosfit.results.domain.CaloriesScore
import com.ctosfit.results.domain.CompletionScore
import com.ctosfit.results.domain.CompletionStatus
import com.ctosfit.results.domain.DistanceScore
import com.ctosfit.results.domain.DistanceUnit
import com.ctosfit.results.domain.LoadScore
import com.ctosfit.results.domain.LoadUnit
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId
import com.ctosfit.results.domain.RepsScore
import com.ctosfit.results.domain.RoundsAndRepsScore
import com.ctosfit.results.domain.TimeOutcome
import com.ctosfit.results.domain.TimeScore
import com.ctosfit.results.domain.WorkoutResult
import com.ctosfit.results.domain.WorkoutScore
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

data class SubmitWorkoutResultRequest(
    val publicationId: String,
    val metricId: String,
    val prescriptionId: String,
    val score: WorkoutScoreRequest,
) {
    fun toCommand(): SubmitWorkoutResultCommand = SubmitWorkoutResultCommand(
        publicationId = PublicationId(publicationId),
        metricId = MetricId(metricId),
        prescriptionId = PrescriptionId(prescriptionId),
        score = score.toDomain(),
    )
}

/** Structured transport score. Only fields belonging to [type] may be supplied. */
data class WorkoutScoreRequest(
    val type: String,
    val amount: BigDecimal? = null,
    val unit: String? = null,
    val elapsedSeconds: Long? = null,
    val timeOutcome: String? = null,
    val reps: Int? = null,
    val rounds: Int? = null,
    val additionalReps: Int? = null,
    val calories: Int? = null,
    val completionStatus: String? = null,
) {
    fun toDomain(): WorkoutScore {
        val scoreType = runCatching { RequestScoreType.valueOf(type) }
            .getOrElse { throw IllegalArgumentException("Unsupported score type") }
        validateFields(scoreType)
        return when (scoreType) {
            RequestScoreType.LOAD -> LoadScore(requireNotNull(amount), enumValue(unit, "load unit"))
            RequestScoreType.TIME -> TimeScore(
                Duration.ofSeconds(requireNotNull(elapsedSeconds)),
                enumValue(timeOutcome, "time outcome"),
            )
            RequestScoreType.REPS -> RepsScore(requireNotNull(reps))
            RequestScoreType.ROUNDS_AND_REPS -> RoundsAndRepsScore(
                requireNotNull(rounds),
                requireNotNull(additionalReps),
            )
            RequestScoreType.DISTANCE -> DistanceScore(requireNotNull(amount), enumValue(unit, "distance unit"))
            RequestScoreType.CALORIES -> CaloriesScore(requireNotNull(calories))
            RequestScoreType.COMPLETION -> CompletionScore(enumValue(completionStatus, "completion status"))
        }
    }

    private fun validateFields(scoreType: RequestScoreType) {
        val supplied = buildSet {
            if (amount != null) add("amount")
            if (unit != null) add("unit")
            if (elapsedSeconds != null) add("elapsedSeconds")
            if (timeOutcome != null) add("timeOutcome")
            if (reps != null) add("reps")
            if (rounds != null) add("rounds")
            if (additionalReps != null) add("additionalReps")
            if (calories != null) add("calories")
            if (completionStatus != null) add("completionStatus")
        }
        require(supplied == scoreType.fields) { "Score fields do not match score type $type" }
    }

    private inline fun <reified T : Enum<T>> enumValue(value: String?, label: String): T {
        val present = requireNotNull(value) { "$label is required" }
        return runCatching { enumValueOf<T>(present) }
            .getOrElse { throw IllegalArgumentException("Unsupported $label") }
    }

    private enum class RequestScoreType(val fields: Set<String>) {
        LOAD(setOf("amount", "unit")),
        TIME(setOf("elapsedSeconds", "timeOutcome")),
        REPS(setOf("reps")),
        ROUNDS_AND_REPS(setOf("rounds", "additionalReps")),
        DISTANCE(setOf("amount", "unit")),
        CALORIES(setOf("calories")),
        COMPLETION(setOf("completionStatus")),
    }
}

data class WorkoutScoreResponse(
    val type: String,
    val amount: BigDecimal? = null,
    val unit: String? = null,
    val elapsedSeconds: Long? = null,
    val timeOutcome: String? = null,
    val reps: Int? = null,
    val rounds: Int? = null,
    val additionalReps: Int? = null,
    val calories: Int? = null,
    val completionStatus: String? = null,
) {
    companion object {
        fun from(score: WorkoutScore): WorkoutScoreResponse = when (score) {
            is LoadScore -> WorkoutScoreResponse("LOAD", amount = score.amount, unit = score.unit.name)
            is TimeScore -> WorkoutScoreResponse(
                "TIME",
                elapsedSeconds = score.elapsed.seconds,
                timeOutcome = score.outcome.name,
            )
            is RepsScore -> WorkoutScoreResponse("REPS", reps = score.reps)
            is RoundsAndRepsScore -> WorkoutScoreResponse(
                "ROUNDS_AND_REPS",
                rounds = score.rounds,
                additionalReps = score.additionalReps,
            )
            is DistanceScore -> WorkoutScoreResponse("DISTANCE", amount = score.amount, unit = score.unit.name)
            is CaloriesScore -> WorkoutScoreResponse("CALORIES", calories = score.calories)
            is CompletionScore -> WorkoutScoreResponse("COMPLETION", completionStatus = score.status.name)
        }
    }
}

data class WorkoutResultResponse(
    val id: String,
    val athleteId: String,
    val publicationId: String,
    val metricId: String,
    val prescriptionId: String,
    val submittedAt: Instant,
    val score: WorkoutScoreResponse,
) {
    companion object {
        fun from(result: WorkoutResult) = WorkoutResultResponse(
            id = result.id.value,
            athleteId = result.athleteId.value,
            publicationId = result.publicationId.value,
            metricId = result.metricId.value,
            prescriptionId = result.prescriptionId.value,
            submittedAt = result.submittedAt,
            score = WorkoutScoreResponse.from(result.score),
        )
    }
}

@RestController
@RequestMapping("/api/athletes/results")
class WorkoutResultController(
    private val submitWorkoutResult: SubmitWorkoutResult,
    private val getMyWorkoutResults: GetMyWorkoutResults,
) {
    @PostMapping
    fun submit(@RequestBody request: SubmitWorkoutResultRequest): ResponseEntity<WorkoutResultResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(WorkoutResultResponse.from(submitWorkoutResult.submit(request.toCommand())))

    @GetMapping
    fun history(): List<WorkoutResultResponse> = getMyWorkoutResults.get().map(WorkoutResultResponse::from)
}

@RestController
@RequestMapping("/api/coaches/results")
class BoxWorkoutResultController(
    private val getBoxWorkoutResults: GetBoxWorkoutResults,
) {
    @GetMapping
    fun boxResults(): List<WorkoutResultResponse> = getBoxWorkoutResults.get().map(WorkoutResultResponse::from)
}

data class PersonalBestResponse(
    val metricId: String,
    val prescriptionId: String,
    val resultId: String,
    val achievedAt: Instant,
    val score: WorkoutScoreResponse,
)

@RestController
@RequestMapping("/api/athletes/personal-bests")
class PersonalBestController(
    private val getMyPersonalBests: GetMyPersonalBests,
) {
    @GetMapping
    fun personalBests(): List<PersonalBestResponse> = getMyPersonalBests.get().map {
        PersonalBestResponse(
            metricId = it.metricId.value,
            prescriptionId = it.prescriptionId.value,
            resultId = it.resultId.value,
            achievedAt = it.achievedAt,
            score = WorkoutScoreResponse.from(it.score),
        )
    }
}

@RestControllerAdvice(assignableTypes = [WorkoutResultController::class])
class WorkoutResultHttpExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(exception: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (exception.message ?: "Invalid workout result")))

    @ExceptionHandler(InvalidWorkoutResultException::class)
    fun invalidResult(exception: InvalidWorkoutResultException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to exception.message.orEmpty()))

    @ExceptionHandler(PublishedWorkoutNotFoundException::class)
    fun publicationNotFound(): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Published workout was not found"))
}
