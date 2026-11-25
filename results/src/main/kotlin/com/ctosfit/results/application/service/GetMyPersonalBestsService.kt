package com.ctosfit.results.application.service

import com.ctosfit.results.application.port.`in`.GetMyPersonalBests
import com.ctosfit.results.application.port.`in`.PersonalBest
import com.ctosfit.results.application.port.out.CurrentAthleteContextProvider
import com.ctosfit.results.application.port.out.WorkoutResultRepository
import com.ctosfit.results.domain.CaloriesScore
import com.ctosfit.results.domain.CompletionScore
import com.ctosfit.results.domain.DistanceScore
import com.ctosfit.results.domain.LoadScore
import com.ctosfit.results.domain.RoundsAndRepsScore
import com.ctosfit.results.domain.ScoredAttempt
import com.ctosfit.results.domain.TimeOutcome
import com.ctosfit.results.domain.TimeScore
import com.ctosfit.results.domain.WorkoutScoreComparison

class GetMyPersonalBestsService(
    private val athleteContextProvider: CurrentAthleteContextProvider,
    private val workoutResultRepository: WorkoutResultRepository,
) : GetMyPersonalBests {
    override fun get(): List<PersonalBest> {
        val context = athleteContextProvider.current()
        val eligibleResults = workoutResultRepository.findByBoxAndAthlete(context.boxId, context.athleteId)
            .filterNot { it.score is TimeScore && it.score.outcome != TimeOutcome.COMPLETED }
        return eligibleResults
            .groupBy { BestKey(it.metricId, it.prescriptionId, it.score.comparabilityKey()) }
            .values
            .mapNotNull { results ->
                val candidates = results.map { ScoredAttempt(it.metricId, it.score) }
                val best = WorkoutScoreComparison.bestOf(candidates) ?: return@mapNotNull null
                val winner = results.first { it.score == best.score }
                PersonalBest(winner.metricId, winner.prescriptionId, winner.id, winner.submittedAt, winner.score)
            }
            .sortedWith(compareBy({ it.metricId.value }, { it.prescriptionId.value }))
    }

    private fun com.ctosfit.results.domain.WorkoutScore.comparabilityKey(): String = when (this) {
        is LoadScore -> "LOAD:${unit.name}"
        is TimeScore -> "TIME:${outcome.name}"
        is com.ctosfit.results.domain.RepsScore -> "REPS"
        is RoundsAndRepsScore -> "ROUNDS_AND_REPS"
        is DistanceScore -> "DISTANCE:${unit.name}"
        is CaloriesScore -> "CALORIES"
        is CompletionScore -> "COMPLETION"
    }

    private data class BestKey(
        val metricId: com.ctosfit.results.domain.MetricId,
        val prescriptionId: com.ctosfit.results.domain.PrescriptionId,
        val scoreSemantics: String,
    )
}
