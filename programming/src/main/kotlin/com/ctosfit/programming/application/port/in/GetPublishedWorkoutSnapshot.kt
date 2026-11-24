package com.ctosfit.programming.application.port.`in`

import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutPublication

/** Read contract used by consumers that need an immutable, tenant-scoped workout view. */
data class PublishedWorkoutSnapshot(
    val boxId: BoxId,
    val publicationId: PublicationId,
    /** Stable exercise/movement identities used to decide whether results are comparable. */
    val metricIds: Set<String>,
    /** Prescription variants available throughout this publication. */
    val prescriptionIds: Set<String>,
    val snapshot: WorkoutPublication,
)

fun interface GetPublishedWorkoutSnapshot {
    fun get(boxId: BoxId, publicationId: PublicationId): PublishedWorkoutSnapshot?
}
