package com.ctosfit.results.domain

import java.time.Instant

/** An athlete's score against one immutable publication and selected prescription. */
data class WorkoutResult internal constructor(
    val id: ResultId,
    val boxId: BoxId,
    val athleteId: AthleteId,
    val publicationId: PublicationId,
    val metricId: MetricId,
    val prescriptionId: PrescriptionId,
    val submittedAt: Instant,
    val score: WorkoutScore,
)
