package com.ctosfit.programming.application.port.`in`

import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.LocalDate

fun interface PublishWorkout {
    fun publish(workoutId: WorkoutId, scheduledFor: LocalDate): WorkoutPublication
}
