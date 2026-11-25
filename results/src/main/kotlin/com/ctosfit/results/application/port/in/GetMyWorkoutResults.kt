package com.ctosfit.results.application.port.`in`

import com.ctosfit.results.domain.WorkoutResult

/** Returns only the authenticated athlete's history in their trusted current box. */
interface GetMyWorkoutResults {
    fun get(): List<WorkoutResult>
}
