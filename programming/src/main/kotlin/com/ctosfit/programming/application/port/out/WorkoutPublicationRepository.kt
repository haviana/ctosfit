package com.ctosfit.programming.application.port.out

import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.LocalDate

interface WorkoutPublicationRepository {
    /** Persist a new immutable snapshot; implementations must not update existing snapshots. */
    fun save(publication: WorkoutPublication): WorkoutPublication

    fun findByBoxAndId(boxId: BoxId, publicationId: PublicationId): WorkoutPublication?

    fun findByBoxAndScheduledDate(boxId: BoxId, scheduledFor: LocalDate): List<WorkoutPublication>
}
