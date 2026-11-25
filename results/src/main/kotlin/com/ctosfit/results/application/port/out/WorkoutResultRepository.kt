package com.ctosfit.results.application.port.out

import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId
import com.ctosfit.results.domain.WorkoutResult

interface WorkoutResultRepository {
    fun save(result: WorkoutResult): WorkoutResult

    /** Both tenant and athlete are mandatory to prevent cross-tenant history reads. */
    fun findByBoxAndAthlete(boxId: BoxId, athleteId: AthleteId): List<WorkoutResult>

    /** Coach view is tenant-scoped; authorization must be resolved before this call. */
    fun findByBox(boxId: BoxId): List<WorkoutResult>
}
