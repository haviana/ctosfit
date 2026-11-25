package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.`in`.GetPublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.`in`.PublishedWorkoutSnapshot
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.PrescriptionVariant

class GetPublishedWorkoutSnapshotService(
    private val publicationRepository: WorkoutPublicationRepository,
) : GetPublishedWorkoutSnapshot {
    override fun get(boxId: BoxId, publicationId: PublicationId): PublishedWorkoutSnapshot? {
        val publication = publicationRepository.findByBoxAndId(boxId, publicationId) ?: return null
        val movements = publication.blocks.flatMap { it.movements }
        val availablePrescriptions = PrescriptionVariant.entries.toSet()
            .filterTo(linkedSetOf()) { variant -> movements.isNotEmpty() && movements.all { variant in it.variants } }

        return PublishedWorkoutSnapshot(
            boxId = publication.boxId,
            publicationId = publication.id,
            metricIds = movements.mapTo(linkedSetOf()) { it.exerciseId.value },
            prescriptionIds = availablePrescriptions.mapTo(linkedSetOf()) { it.name },
            snapshot = publication,
        )
    }
}
