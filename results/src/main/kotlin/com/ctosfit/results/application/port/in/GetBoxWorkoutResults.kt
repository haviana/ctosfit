package com.ctosfit.results.application.port.`in`

import com.ctosfit.results.domain.WorkoutResult

fun interface GetBoxWorkoutResults {
    fun get(): List<WorkoutResult>
}
