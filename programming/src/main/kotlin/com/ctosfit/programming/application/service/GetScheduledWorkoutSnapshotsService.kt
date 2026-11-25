package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.`in`.GetScheduledWorkoutSnapshots
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.LocalDate

class GetScheduledWorkoutSnapshotsService(
    private val workoutPublicationRepository: WorkoutPublicationRepository,
) : GetScheduledWorkoutSnapshots {
    override fun get(boxId: BoxId, scheduledFor: LocalDate): List<WorkoutPublication> =
        workoutPublicationRepository.findByBoxAndScheduledDate(boxId, scheduledFor)
}
