package com.ctosfit.exercise.application.port.out

import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.TenantId

/** Every repository operation is scoped by tenant, including ID lookups and search. */
interface ExerciseRepository {
    fun save(exercise: Exercise): Exercise

    fun findByTenantAndId(tenantId: TenantId, exerciseId: ExerciseId): Exercise?

    fun searchByTenant(tenantId: TenantId, query: String, includeArchived: Boolean): List<Exercise>
}
