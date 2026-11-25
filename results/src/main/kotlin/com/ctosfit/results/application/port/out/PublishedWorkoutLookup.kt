package com.ctosfit.results.application.port.out

import com.ctosfit.results.domain.BoxId
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId

/** Minimal read contract supplied by Programming; this module does not access its repositories. */
data class PublishedWorkoutDetails(
    val boxId: BoxId,
    val publicationId: PublicationId,
    val metricIds: Set<MetricId>,
    val prescriptionIds: Set<PrescriptionId>,
)

fun interface PublishedWorkoutLookup {
    /** Lookup must be scoped by box so guessed publication IDs cannot cross tenants. */
    fun findPublishedWorkout(boxId: BoxId, publicationId: PublicationId): PublishedWorkoutDetails?
}
