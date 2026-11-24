package com.ctosfit.programming.domain

import java.util.Collections

enum class BlockIntent {
    WARM_UP,
    STRENGTH,
    CONDITIONING,
    SKILL,
    ACCESSORY,
}

sealed interface WorkoutFormat {
    data class Amrap(val durationSeconds: Int) : WorkoutFormat {
        init {
            require(durationSeconds > 0) { "AMRAP duration must be greater than zero" }
        }
    }

    data class Emom(val intervalSeconds: Int, val intervalCount: Int) : WorkoutFormat {
        init {
            require(intervalSeconds > 0) { "EMOM interval must be greater than zero" }
            require(intervalCount > 0) { "EMOM interval count must be greater than zero" }
        }
    }

    data class ForTime(val timeCapSeconds: Int? = null) : WorkoutFormat {
        init {
            require(timeCapSeconds == null || timeCapSeconds > 0) { "Time cap must be greater than zero" }
        }
    }

    data class Tabata(val workSeconds: Int, val restSeconds: Int, val rounds: Int) : WorkoutFormat {
        init {
            require(workSeconds > 0 && restSeconds > 0 && rounds > 0) { "Tabata intervals must be positive" }
        }
    }

    data class Intervals(val rounds: Int, val workSeconds: Int, val restSeconds: Int) : WorkoutFormat {
        init {
            require(rounds > 0 && workSeconds > 0 && restSeconds > 0) { "Interval values must be positive" }
        }
    }

    data class RoundsAndReps(val rounds: Int? = null) : WorkoutFormat {
        init {
            require(rounds == null || rounds > 0) { "Rounds must be greater than zero" }
        }
    }

    data class SetsAndReps(val sets: Int, val restSeconds: Int? = null) : WorkoutFormat {
        init {
            require(sets > 0) { "Set count must be greater than zero" }
            require(restSeconds == null || restSeconds > 0) { "Rest time must be greater than zero" }
        }
    }

    data class Chipper(val timeCapSeconds: Int? = null) : WorkoutFormat {
        init {
            require(timeCapSeconds == null || timeCapSeconds > 0) { "Time cap must be greater than zero" }
        }
    }
}

class WorkoutBlock(
    val id: WorkoutBlockId,
    val title: String,
    val intent: BlockIntent,
    val format: WorkoutFormat?,
    movements: List<MovementPrescription>,
    val instructions: String? = null,
) {
    val movements: List<MovementPrescription> =
        Collections.unmodifiableList(movements.map(MovementPrescription::snapshotCopy))

    init {
        require(title.isNotBlank()) { "Workout block title must not be blank" }
        require(this.movements.isNotEmpty()) { "A workout block must contain at least one movement" }
    }

    internal fun snapshotCopy(): WorkoutBlock =
        WorkoutBlock(id, title, intent, format, movements, instructions)
}
