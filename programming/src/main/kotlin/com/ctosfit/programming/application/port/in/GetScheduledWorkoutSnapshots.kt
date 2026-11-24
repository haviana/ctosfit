package com.ctosfit.programming.application.port.`in`

import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.LocalDate

/** Lists the exact published workout snapshots scheduled for one box-local date. */
fun interface GetScheduledWorkoutSnapshots {
    fun get(boxId: BoxId, scheduledFor: LocalDate): List<WorkoutPublication>
}
