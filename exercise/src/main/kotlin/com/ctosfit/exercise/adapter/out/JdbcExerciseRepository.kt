package com.ctosfit.exercise.adapter.out

import com.ctosfit.exercise.application.port.out.ExerciseRepository
import com.ctosfit.exercise.domain.Exercise
import com.ctosfit.exercise.domain.ExerciseCategory
import com.ctosfit.exercise.domain.ExerciseId
import com.ctosfit.exercise.domain.ExerciseName
import com.ctosfit.exercise.domain.TenantId
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

/** PostgreSQL adapter; every query and mutation carries the verified box scope. */
class JdbcExerciseRepository(
    private val dataSource: DataSource,
) : ExerciseRepository {
    override fun save(exercise: Exercise): Exercise {
        val boxUuid = UUID.fromString(exercise.tenantId.value)
        val exerciseUuid = UUID.fromString(exercise.id.value)
        dataSource.connection.use { connection ->
            val previousAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                connection.prepareStatement(
                    """
                    INSERT INTO exercises (id, box_id, name, category, instructions, media_reference, archived_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id, box_id) DO UPDATE SET
                        name = EXCLUDED.name,
                        category = EXCLUDED.category,
                        instructions = EXCLUDED.instructions,
                        media_reference = EXCLUDED.media_reference,
                        archived_at = EXCLUDED.archived_at
                    """.trimIndent(),
                ).use { statement ->
                    statement.setObject(1, exerciseUuid)
                    statement.setObject(2, boxUuid)
                    statement.setString(3, exercise.name.value)
                    statement.setString(4, exercise.category.value)
                    statement.setString(5, exercise.instructions)
                    statement.setString(6, exercise.mediaReference)
                    statement.setTimestamp(7, exercise.archivedAt?.let(Timestamp::from))
                    statement.executeUpdate()
                }

                connection.prepareStatement("DELETE FROM exercise_equipment WHERE box_id = ? AND exercise_id = ?")
                    .use { statement ->
                        statement.setObject(1, boxUuid)
                        statement.setObject(2, exerciseUuid)
                        statement.executeUpdate()
                    }
                connection.prepareStatement(
                    "INSERT INTO exercise_equipment (exercise_id, box_id, equipment) VALUES (?, ?, ?)",
                ).use { statement ->
                    exercise.equipment.sorted().forEach { equipment ->
                        statement.setObject(1, exerciseUuid)
                        statement.setObject(2, boxUuid)
                        statement.setString(3, equipment)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            } finally {
                connection.autoCommit = previousAutoCommit
            }
        }
        return exercise
    }

    override fun findByTenantAndId(tenantId: TenantId, exerciseId: ExerciseId): Exercise? =
        query(tenantId, exerciseId, "") { rows -> rows.firstOrNull() }

    override fun searchByTenant(tenantId: TenantId, query: String, includeArchived: Boolean): List<Exercise> =
        query(tenantId, null, query, includeArchived) { it }

    private fun <T> query(
        tenantId: TenantId,
        exerciseId: ExerciseId?,
        query: String,
        includeArchived: Boolean = true,
        result: (List<Exercise>) -> T,
    ): T {
        val boxUuid = UUID.fromString(tenantId.value)
        val exerciseUuid = exerciseId?.let { UUID.fromString(it.value) }
        val sql = buildString {
            append(
                """
                SELECT e.id, e.box_id, e.name, e.category, e.instructions, e.media_reference,
                       e.archived_at, ee.equipment
                FROM exercises e
                LEFT JOIN exercise_equipment ee ON ee.box_id = e.box_id AND ee.exercise_id = e.id
                WHERE e.box_id = ?
                """.trimIndent(),
            )
            if (exerciseUuid != null) append(" AND e.id = ?")
            if (!includeArchived) append(" AND e.archived_at IS NULL")
            if (query.isNotBlank()) append(" AND (LOWER(e.name) LIKE ? OR LOWER(e.category) LIKE ?)")
            append(" ORDER BY LOWER(e.name), e.id, ee.equipment")
        }

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                var index = 1
                statement.setObject(index++, boxUuid)
                exerciseUuid?.let { statement.setObject(index++, it) }
                if (query.isNotBlank()) {
                    val pattern = "%${query.trim().lowercase()}%"
                    statement.setString(index++, pattern)
                    statement.setString(index, pattern)
                }
                statement.executeQuery().use { rows -> result(rows.toExercises()) }
            }
        }
    }

    private fun ResultSet.toExercises(): List<Exercise> {
        val grouped = linkedMapOf<UUID, ExerciseRow>()
        while (next()) {
            val id = getObject("id", UUID::class.java)
            val row = grouped.getOrPut(id) {
                ExerciseRow(
                    id = id,
                    boxId = getObject("box_id", UUID::class.java),
                    name = getString("name"),
                    category = getString("category"),
                    instructions = getString("instructions"),
                    mediaReference = getString("media_reference"),
                    archivedAt = getTimestamp("archived_at")?.toInstant(),
                )
            }
            getString("equipment")?.let(row.equipment::add)
        }
        return grouped.values.map(ExerciseRow::toDomain)
    }

    private data class ExerciseRow(
        val id: UUID,
        val boxId: UUID,
        val name: String,
        val category: String,
        val instructions: String?,
        val mediaReference: String?,
        val archivedAt: java.time.Instant?,
        val equipment: MutableSet<String> = linkedSetOf(),
    ) {
        fun toDomain(): Exercise {
            var exercise = Exercise.create(
                id = ExerciseId(id.toString()),
                tenantId = TenantId(boxId.toString()),
                name = ExerciseName(name),
                category = ExerciseCategory(category),
                equipment = equipment,
                instructions = instructions,
                mediaReference = mediaReference,
            )
            archivedAt?.let { exercise = exercise.archive(it) }
            return exercise
        }
    }
}

