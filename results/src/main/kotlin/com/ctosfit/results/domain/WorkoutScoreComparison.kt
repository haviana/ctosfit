package com.ctosfit.results.domain

/** Ordering of the first score relative to the second for personal-record purposes. */
enum class ScoreOrdering {
    LEFT_BETTER,
    EQUAL,
    RIGHT_BETTER,
}

/** One score plus the workout/benchmark identity that gives it comparison meaning. */
data class ScoredAttempt(
    val metricId: MetricId,
    val score: WorkoutScore,
)

object WorkoutScoreComparison {
    /** True only when metric identity, score type, units, and time outcome match. */
    fun areComparable(left: ScoredAttempt, right: ScoredAttempt): Boolean =
        left.metricId == right.metricId && areComparableValues(left.score, right.score)

    private fun areComparableValues(left: WorkoutScore, right: WorkoutScore): Boolean = when {
        left is LoadScore && right is LoadScore -> left.unit == right.unit
        left is TimeScore && right is TimeScore ->
            left.outcome == TimeOutcome.COMPLETED && right.outcome == left.outcome
        left is RepsScore && right is RepsScore -> true
        left is RoundsAndRepsScore && right is RoundsAndRepsScore -> true
        left is DistanceScore && right is DistanceScore -> left.unit == right.unit
        left is CaloriesScore && right is CaloriesScore -> true
        left is CompletionScore && right is CompletionScore -> true
        else -> false
    }

    /** Compares the first score with the second; incompatible values are rejected. */
    fun compareForBest(left: ScoredAttempt, right: ScoredAttempt): ScoreOrdering {
        require(areComparable(left, right)) { "Workout scores are not comparable" }
        val leftScore = left.score
        val rightScore = right.score

        return when {
            leftScore is LoadScore && rightScore is LoadScore -> higherIsBetter(leftScore.amount.compareTo(rightScore.amount))
            leftScore is TimeScore && rightScore is TimeScore -> lowerIsBetter(leftScore.elapsed.compareTo(rightScore.elapsed))
            leftScore is RepsScore && rightScore is RepsScore -> higherIsBetter(leftScore.reps.compareTo(rightScore.reps))
            leftScore is RoundsAndRepsScore && rightScore is RoundsAndRepsScore ->
                higherIsBetter(compareValuesBy(leftScore, rightScore, RoundsAndRepsScore::rounds, RoundsAndRepsScore::additionalReps))
            leftScore is DistanceScore && rightScore is DistanceScore -> higherIsBetter(leftScore.amount.compareTo(rightScore.amount))
            leftScore is CaloriesScore && rightScore is CaloriesScore -> higherIsBetter(leftScore.calories.compareTo(rightScore.calories))
            leftScore is CompletionScore && rightScore is CompletionScore ->
                higherIsBetter(completionValue(leftScore.status).compareTo(completionValue(rightScore.status)))
            else -> error("Comparable score pair was not handled")
        }
    }

    /** Returns the best eligible attempt for one metric; non-finished times are not PR candidates. */
    fun bestOf(attempts: Iterable<ScoredAttempt>): ScoredAttempt? {
        var best: ScoredAttempt? = null
        for (candidate in attempts) {
            if (candidate.score is TimeScore && candidate.score.outcome != TimeOutcome.COMPLETED) continue
            val currentBest = best
            if (currentBest == null || compareForBest(candidate, currentBest) == ScoreOrdering.LEFT_BETTER) {
                best = candidate
            }
        }
        return best
    }

    private fun higherIsBetter(comparison: Int): ScoreOrdering = when {
        comparison > 0 -> ScoreOrdering.LEFT_BETTER
        comparison < 0 -> ScoreOrdering.RIGHT_BETTER
        else -> ScoreOrdering.EQUAL
    }

    private fun lowerIsBetter(comparison: Int): ScoreOrdering = when {
        comparison < 0 -> ScoreOrdering.LEFT_BETTER
        comparison > 0 -> ScoreOrdering.RIGHT_BETTER
        else -> ScoreOrdering.EQUAL
    }

    private fun completionValue(status: CompletionStatus): Int = when (status) {
        CompletionStatus.NOT_COMPLETED -> 0
        CompletionStatus.COMPLETED -> 1
    }
}
