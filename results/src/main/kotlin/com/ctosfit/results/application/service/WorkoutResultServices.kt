package com.ctosfit.results.application.service

import com.ctosfit.results.application.port.`in`.GetMyWorkoutResults
import com.ctosfit.results.application.port.`in`.GetBoxWorkoutResults
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResult
import com.ctosfit.results.application.port.`in`.SubmitWorkoutResultCommand
import com.ctosfit.results.application.port.out.CurrentAthleteContextProvider
import com.ctosfit.results.application.port.out.CurrentCoachBoxProvider
import com.ctosfit.results.application.port.out.PublishedWorkoutLookup
import com.ctosfit.results.application.port.out.WorkoutResultRepository
import com.ctosfit.results.domain.ResultId
import com.ctosfit.results.domain.WorkoutResult
import java.time.Clock
import java.util.UUID

class PublishedWorkoutNotFoundException : RuntimeException("Published workout was not found")

class InvalidWorkoutResultException(message: String) : RuntimeException(message)

class SubmitWorkoutResultService(
    private val athleteContextProvider: CurrentAthleteContextProvider,
    private val publishedWorkoutLookup: PublishedWorkoutLookup,
    private val workoutResultRepository: WorkoutResultRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val newResultId: () -> ResultId = { ResultId(UUID.randomUUID().toString()) },
) : SubmitWorkoutResult {
    override fun submit(command: SubmitWorkoutResultCommand): WorkoutResult {
        val context = athleteContextProvider.current()
        val publication = publishedWorkoutLookup.findPublishedWorkout(context.boxId, command.publicationId)
            ?: throw PublishedWorkoutNotFoundException()

        if (publication.boxId != context.boxId || publication.publicationId != command.publicationId) {
            throw PublishedWorkoutNotFoundException()
        }
        if (command.metricId !in publication.metricIds) {
            throw InvalidWorkoutResultException("Metric does not belong to the published workout")
        }
        if (command.prescriptionId !in publication.prescriptionIds) {
            throw InvalidWorkoutResultException("Prescription does not belong to the published workout")
        }

        return workoutResultRepository.save(
            WorkoutResult(
                id = newResultId(),
                boxId = context.boxId,
                athleteId = context.athleteId,
                publicationId = publication.publicationId,
                metricId = command.metricId,
                prescriptionId = command.prescriptionId,
                submittedAt = clock.instant(),
                score = command.score,
            ),
        )
    }
}

class GetMyWorkoutResultsService(
    private val athleteContextProvider: CurrentAthleteContextProvider,
    private val workoutResultRepository: WorkoutResultRepository,
) : GetMyWorkoutResults {
    override fun get(): List<WorkoutResult> {
        val context = athleteContextProvider.current()
        return workoutResultRepository.findByBoxAndAthlete(context.boxId, context.athleteId)
    }
}

class GetBoxWorkoutResultsService(
    private val coachBoxProvider: CurrentCoachBoxProvider,
    private val workoutResultRepository: WorkoutResultRepository,
) : GetBoxWorkoutResults {
    override fun get(): List<WorkoutResult> = workoutResultRepository.findByBox(coachBoxProvider.currentBoxId())
}
