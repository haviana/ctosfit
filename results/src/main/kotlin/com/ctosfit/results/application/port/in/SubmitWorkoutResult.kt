package com.ctosfit.results.application.port.`in`

import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId
import com.ctosfit.results.domain.WorkoutResult
import com.ctosfit.results.domain.WorkoutScore

/** Client-supplied score data. Athlete and box identity are resolved from trusted context. */
data class SubmitWorkoutResultCommand(
    val publicationId: PublicationId,
    val metricId: MetricId,
    val prescriptionId: PrescriptionId,
    val score: WorkoutScore,
)

interface SubmitWorkoutResult {
    fun submit(command: SubmitWorkoutResultCommand): WorkoutResult
}
