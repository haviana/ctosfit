package com.ctosfit.exercise.application.service

import com.ctosfit.exercise.application.port.`in`.SaveExerciseCommand
import com.ctosfit.exercise.application.port.out.CurrentTenantProvider
import com.ctosfit.exercise.application.port.out.ExerciseRepository
import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseCategory
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.ExerciseName
import com.ctosfit.exercise.domain.TenantId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ManageExercisesServiceTest {
    private val firstTenant = TenantId("box-1")
    private val otherTenant = TenantId("box-2")
    private val repository = InMemoryExerciseRepository()
    private var currentTenant = firstTenant
    private val service = ManageExercisesService(repository, CurrentTenantProvider { currentTenant })

    @Test
    fun `search and identifier lookup remain scoped to tenant`() {
        val exercise = service.create(command("Deadlift"))
        currentTenant = otherTenant
        service.create(command("Deadlift"))

        currentTenant = firstTenant
        assertEquals(listOf(exercise), service.search("Deadlift"))
        currentTenant = otherTenant
        assertFailsWith<ExerciseNotFoundException> {
            service.update(exercise.id, command("Deadlift updated"))
        }
    }

    @Test
    fun `archived exercise remains available by stable ID but is hidden from normal search`() {
        val exercise = service.create(command("Row"))
        val archived = service.archive(exercise.id, Instant.parse("2026-11-24T12:00:00Z"))

        assertEquals(exercise.id, archived.id)
        assertNull(service.search("Row").firstOrNull())
        assertEquals(archived, repository.findByTenantAndId(firstTenant, exercise.id))
        assertEquals(listOf(archived), service.search("Row", includeArchived = true))
        assertFailsWith<IllegalStateException> {
            archived.update(
                ExerciseName("Row updated"),
                ExerciseCategory("monostructural"),
                emptySet(),
                null,
                null,
            )
        }
    }

    private fun command(name: String) = SaveExerciseCommand(
        name = ExerciseName(name),
        category = ExerciseCategory("weightlifting"),
        equipment = setOf("barbell"),
    )

    private class InMemoryExerciseRepository : ExerciseRepository {
        private val exercises = mutableMapOf<Pair<TenantId, ExerciseId>, Exercise>()

        override fun save(exercise: Exercise): Exercise {
            exercises[exercise.tenantId to exercise.id] = exercise
            return exercise
        }

        override fun findByTenantAndId(tenantId: TenantId, exerciseId: ExerciseId): Exercise? =
            exercises[tenantId to exerciseId]

        override fun searchByTenant(tenantId: TenantId, query: String, includeArchived: Boolean): List<Exercise> =
            exercises.values.filter {
                it.tenantId == tenantId &&
                    it.name.value.contains(query, ignoreCase = true) &&
                    (includeArchived || it.archivedAt == null)
            }
    }
}
