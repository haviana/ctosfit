package com.ctosfit.results.application.port.`in`

import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.ResultId
import com.ctosfit.results.domain.WorkoutScore
import java.time.Instant

data class PersonalBest(
    val metricId: MetricId,
    val prescriptionId: PrescriptionId,
    val resultId: ResultId,
    val achievedAt: Instant,
    val score: WorkoutScore,
)

fun interface GetMyPersonalBests {
    fun get(): List<PersonalBest>
}
