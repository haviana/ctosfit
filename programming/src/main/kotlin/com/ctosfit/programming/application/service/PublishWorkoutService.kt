package com.ctosfit.programming.application.service

import com.ctosfit.programming.application.port.`in`.PublishWorkout
import com.ctosfit.programming.application.port.out.CurrentCoachContextProvider
import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class PublishWorkoutService(
    private val draftRepository: WorkoutDraftRepository,
    private val publicationRepository: WorkoutPublicationRepository,
    private val currentCoachContextProvider: CurrentCoachContextProvider,
    private val clock: Clock = Clock.systemUTC(),
) : PublishWorkout {
    override fun publish(workoutId: WorkoutId, scheduledFor: LocalDate): WorkoutPublication {
        val context = currentCoachContextProvider.currentCoachContext()
        val draft = draftRepository.findByBoxAndId(context.boxId, workoutId) ?: throw WorkoutDraftNotFoundException()
        val publication = draft.publish(
            publicationId = PublicationId(UUID.randomUUID().toString()),
            scheduledFor = scheduledFor,
            publishedAt = clock.instant(),
            publishedBy = context.coachId,
        )
        return publicationRepository.save(publication)
    }
}
