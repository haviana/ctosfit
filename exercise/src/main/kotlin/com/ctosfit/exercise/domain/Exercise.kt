package com.ctosfit.exercise.domain

import java.time.Instant

@JvmInline
value class ExerciseId(val value: String) {
    init {
        require(value.isNotBlank()) { "Exercise ID must not be blank" }
    }
}

/** Tenant ID is supplied by the application boundary from verified membership context. */
@JvmInline
value class TenantId(val value: String) {
    init {
        require(value.isNotBlank()) { "Tenant ID must not be blank" }
    }
}

@JvmInline
value class ExerciseName(val value: String) {
    init {
        require(value.isNotBlank()) { "Exercise name must not be blank" }
        require(value.length <= 120) { "Exercise name must be 120 characters or fewer" }
    }
}

@JvmInline
value class ExerciseCategory(val value: String) {
    init {
        require(value.isNotBlank()) { "Exercise category must not be blank" }
        require(value.length <= 80) { "Exercise category must be 80 characters or fewer" }
    }
}

/** Stable exercise definition; archiving preserves its identity for historical references. */
class Exercise private constructor(
    val id: ExerciseId,
    val tenantId: TenantId,
    val name: ExerciseName,
    val category: ExerciseCategory,
    val equipment: Set<String>,
    val instructions: String?,
    val mediaReference: String?,
    val archivedAt: Instant?,
) {
    fun update(
        name: ExerciseName,
        category: ExerciseCategory,
        equipment: Set<String>,
        instructions: String?,
        mediaReference: String?,
    ): Exercise {
        check(archivedAt == null) { "Archived exercises cannot be edited" }
        validateEquipment(equipment)
        return Exercise(id, tenantId, name, category, equipment.toSet(), instructions, mediaReference, null)
    }

    fun archive(at: Instant): Exercise =
        if (archivedAt == null) Exercise(id, tenantId, name, category, equipment, instructions, mediaReference, at)
        else this

    companion object {
        fun create(
            id: ExerciseId,
            tenantId: TenantId,
            name: ExerciseName,
            category: ExerciseCategory,
            equipment: Set<String> = emptySet(),
            instructions: String? = null,
            mediaReference: String? = null,
        ): Exercise {
            validateEquipment(equipment)
            return Exercise(id, tenantId, name, category, equipment.toSet(), instructions, mediaReference, null)
        }

        private fun validateEquipment(equipment: Set<String>) {
            require(equipment.none { it.isBlank() }) { "Equipment entries must not be blank" }
            require(equipment.all { it.length <= 120 }) { "Equipment entries must be 120 characters or fewer" }
        }
    }
}
