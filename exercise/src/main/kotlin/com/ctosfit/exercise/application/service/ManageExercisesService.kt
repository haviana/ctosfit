package com.ctosfit.exercise.application.service

import com.ctosfit.exercise.application.port.`in`.ManageExercises
import com.ctosfit.exercise.application.port.`in`.SaveExerciseCommand
import com.ctosfit.exercise.application.port.out.CurrentTenantProvider
import com.ctosfit.exercise.application.port.out.ExerciseRepository
import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseId
import java.time.Instant
import java.util.UUID

class ExerciseNotFoundException : RuntimeException("Exercise was not found")

class ManageExercisesService(
    private val exerciseRepository: ExerciseRepository,
    private val currentTenantProvider: CurrentTenantProvider,
) : ManageExercises {
    override fun create(command: SaveExerciseCommand): Exercise =
        exerciseRepository.save(
            Exercise.create(
                id = ExerciseId(UUID.randomUUID().toString()),
                tenantId = currentTenantProvider.currentTenantId(),
                name = command.name,
                category = command.category,
                equipment = command.equipment,
                instructions = command.instructions,
                mediaReference = command.mediaReference,
            ),
        )

    override fun update(exerciseId: ExerciseId, command: SaveExerciseCommand): Exercise =
        exerciseRepository.findByTenantAndId(currentTenantProvider.currentTenantId(), exerciseId)
            ?.update(command.name, command.category, command.equipment, command.instructions, command.mediaReference)
            ?.let(exerciseRepository::save)
            ?: throw ExerciseNotFoundException()

    override fun archive(exerciseId: ExerciseId, at: Instant): Exercise =
        exerciseRepository.findByTenantAndId(currentTenantProvider.currentTenantId(), exerciseId)
            ?.archive(at)
            ?.let(exerciseRepository::save)
            ?: throw ExerciseNotFoundException()

    override fun search(query: String, includeArchived: Boolean): List<Exercise> =
        exerciseRepository.searchByTenant(currentTenantProvider.currentTenantId(), query.trim(), includeArchived)
}
