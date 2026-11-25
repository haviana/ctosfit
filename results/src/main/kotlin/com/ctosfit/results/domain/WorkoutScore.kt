package com.ctosfit.results.domain

import java.math.BigDecimal
import java.time.Duration

/** Explicit score data; score type and units are part of the stored value. */
sealed interface WorkoutScore

enum class LoadUnit {
    KILOGRAM,
    POUND,
}

data class LoadScore(
    val amount: BigDecimal,
    val unit: LoadUnit,
) : WorkoutScore {
    init {
        require(amount.signum() >= 0) { "Load must not be negative" }
    }
}

data class TimeScore(
    /** Elapsed time, or elapsed time at the cap / when the athlete stopped. */
    val elapsed: Duration,
    val outcome: TimeOutcome,
) : WorkoutScore {
    init {
        require(!elapsed.isNegative) { "Elapsed time must not be negative" }
    }
}

enum class TimeOutcome {
    COMPLETED,
    TIME_CAP,
    DID_NOT_FINISH,
}

data class RepsScore(val reps: Int) : WorkoutScore {
    init {
        require(reps >= 0) { "Reps must not be negative" }
    }
}

data class RoundsAndRepsScore(
    val rounds: Int,
    val additionalReps: Int,
) : WorkoutScore {
    init {
        require(rounds >= 0) { "Rounds must not be negative" }
        require(additionalReps >= 0) { "Additional reps must not be negative" }
    }
}

enum class DistanceUnit {
    METER,
    KILOMETER,
    MILE,
    FOOT,
}

data class DistanceScore(
    val amount: BigDecimal,
    val unit: DistanceUnit,
) : WorkoutScore {
    init {
        require(amount.signum() >= 0) { "Distance must not be negative" }
    }
}

data class CaloriesScore(val calories: Int) : WorkoutScore {
    init {
        require(calories >= 0) { "Calories must not be negative" }
    }
}

data class CompletionScore(val status: CompletionStatus) : WorkoutScore

enum class CompletionStatus {
    COMPLETED,
    NOT_COMPLETED,
}
