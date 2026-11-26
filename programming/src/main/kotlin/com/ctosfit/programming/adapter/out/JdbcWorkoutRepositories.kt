package com.ctosfit.programming.adapter.out

import com.ctosfit.programming.application.port.out.WorkoutDraftRepository
import com.ctosfit.programming.application.port.out.WorkoutPublicationRepository
import java.time.LocalDate
import com.ctosfit.programming.domain.BlockIntent
import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.CoachId
import com.ctosfit.programming.domain.ExerciseId
import com.ctosfit.programming.domain.MovementPrescription
import com.ctosfit.programming.domain.MovementTarget
import com.ctosfit.programming.domain.MovementUnit
import com.ctosfit.programming.domain.PrescriptionVariant
import com.ctosfit.programming.domain.PublicationId
import com.ctosfit.programming.domain.WorkoutBlock
import com.ctosfit.programming.domain.WorkoutBlockId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId
import com.ctosfit.programming.domain.WorkoutPublication
import com.ctosfit.programming.domain.WorkoutFormat
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.sql.DataSource

/** PostgreSQL persistence for drafts and immutable publication snapshots. */
class JdbcWorkoutRepositories(
    private val dataSource: DataSource,
) : WorkoutDraftRepository, WorkoutPublicationRepository {
    private val mapper = jacksonObjectMapper()

    override fun findByBoxAndId(boxId: BoxId, workoutId: WorkoutId): WorkoutDraft? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "SELECT id, box_id, title, version, content_json FROM workout_drafts WHERE box_id = ? AND id = ?",
            ).use { statement ->
                statement.setObject(1, uuid(boxId.value))
                statement.setObject(2, uuid(workoutId.value))
                statement.executeQuery().use { rows ->
                    if (!rows.next()) null else rows.toDraft()
                }
            }
        }

    override fun save(draft: WorkoutDraft): WorkoutDraft {
        val document = WorkoutDocument.from(draft.title, draft.version, draft.blocks)
        val json = mapper.writeValueAsString(document)
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO workout_drafts (id, box_id, title, version, content_json)
                VALUES (?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id, box_id) DO UPDATE SET
                    title = EXCLUDED.title,
                    version = EXCLUDED.version,
                    content_json = EXCLUDED.content_json,
                    updated_at = CURRENT_TIMESTAMP
                WHERE workout_drafts.version <= EXCLUDED.version
                  AND (workout_drafts.version < EXCLUDED.version
                       OR workout_drafts.content_json = EXCLUDED.content_json)
                """.trimIndent(),
            ).use { statement ->
                statement.setObject(1, uuid(draft.id.value))
                statement.setObject(2, uuid(draft.boxId.value))
                statement.setString(3, draft.title)
                statement.setInt(4, draft.version)
                statement.setString(5, json)
                if (statement.executeUpdate() != 1) {
                    throw IllegalStateException("A newer workout draft version already exists")
                }
            }
        }
        return draft
    }

    override fun save(publication: WorkoutPublication): WorkoutPublication {
        val document = WorkoutDocument.from(publication.title, publication.version, publication.blocks)
        val json = mapper.writeValueAsString(document)
        dataSource.connection.use { connection ->
            val previousAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                connection.prepareStatement(
                    """
                    INSERT INTO workout_publications
                        (id, box_id, workout_id, version, title, scheduled_for, published_at, published_by, content_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setObject(1, uuid(publication.id.value))
                    statement.setObject(2, uuid(publication.boxId.value))
                    statement.setObject(3, uuid(publication.workoutId.value))
                    statement.setInt(4, publication.version)
                    statement.setString(5, publication.title)
                    statement.setDate(6, Date.valueOf(publication.scheduledFor))
                    statement.setObject(7, OffsetDateTime.ofInstant(publication.publishedAt, ZoneOffset.UTC))
                    statement.setString(8, publication.publishedBy.value)
                    statement.setString(9, json)
                    statement.executeUpdate()
                }
                insertPublicationMetrics(connection, publication)
                insertPublicationPrescriptions(connection, publication)
                connection.commit()
            } catch (exception: Exception) {
                connection.rollback()
                if (exception is SQLException && exception.sqlState == "23503") {
                    throw IllegalArgumentException("The publication references an exercise unavailable in this box")
                }
                throw exception
            } finally {
                connection.autoCommit = previousAutoCommit
            }
        }
        return publication
    }

    override fun findByBoxAndId(boxId: BoxId, publicationId: PublicationId): WorkoutPublication? =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, box_id, workout_id, version, title, scheduled_for,
                       published_at, published_by, content_json
                FROM workout_publications
                WHERE box_id = ? AND id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setObject(1, uuid(boxId.value))
                statement.setObject(2, uuid(publicationId.value))
                statement.executeQuery().use { rows ->
                    if (!rows.next()) null else rows.toPublication()
                }
            }
        }

    override fun findByBoxAndScheduledDate(boxId: BoxId, scheduledFor: LocalDate): List<WorkoutPublication> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT id, box_id, workout_id, version, title, scheduled_for,
                       published_at, published_by, content_json
                FROM workout_publications
                WHERE box_id = ? AND scheduled_for = ?
                ORDER BY published_at DESC, version DESC, id DESC
                """.trimIndent(),
            ).use { statement ->
                statement.setObject(1, uuid(boxId.value))
                statement.setDate(2, Date.valueOf(scheduledFor))
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) add(rows.toPublication())
                    }
                }
            }
        }

    private fun insertPublicationMetrics(connection: Connection, publication: WorkoutPublication) {
        val metrics = publication.blocks.flatMap { block -> block.movements.map { it.exerciseId.value } }.toSet()
        connection.prepareStatement(
            "INSERT INTO publication_metrics (box_id, publication_id, metric_id) VALUES (?, ?, ?)",
        ).use { statement ->
            metrics.sorted().forEach { metricId ->
                statement.setObject(1, uuid(publication.boxId.value))
                statement.setObject(2, uuid(publication.id.value))
                statement.setObject(3, uuid(metricId))
                statement.addBatch()
            }
            if (metrics.isNotEmpty()) statement.executeBatch()
        }
    }

    private fun insertPublicationPrescriptions(connection: Connection, publication: WorkoutPublication) {
        val movements = publication.blocks.flatMap { it.movements }
        val prescriptionIds = PrescriptionVariant.entries
            .filter { variant -> movements.isNotEmpty() && movements.all { variant in it.variants } }
            .map { it.name }
        connection.prepareStatement(
            "INSERT INTO publication_prescriptions (box_id, publication_id, prescription_id) VALUES (?, ?, ?)",
        ).use { statement ->
            prescriptionIds.forEach { prescriptionId ->
                statement.setObject(1, uuid(publication.boxId.value))
                statement.setObject(2, uuid(publication.id.value))
                statement.setString(3, prescriptionId)
                statement.addBatch()
            }
            if (prescriptionIds.isNotEmpty()) statement.executeBatch()
        }
    }

    private fun ResultSet.toDraft(): WorkoutDraft {
        val draftId = WorkoutId(getObject("id", UUID::class.java).toString())
        val boxId = BoxId(getObject("box_id", UUID::class.java).toString())
        val title = getString("title")
        val version = getInt("version")
        val document = mapper.readValue<WorkoutDocument>(getString("content_json"))
        check(document.schemaVersion == WorkoutDocument.CURRENT_SCHEMA_VERSION) {
            "Unsupported persisted workout draft schema"
        }
        check(document.title == title && document.version == version) {
            "Persisted workout draft metadata does not match its content"
        }
        return WorkoutDraft(draftId, boxId, title, document.blocks.map(WorkoutBlockDocument::toDomain), version)
    }

    private fun ResultSet.toPublication(): WorkoutPublication {
        val publicationId = PublicationId(getObject("id", UUID::class.java).toString())
        val boxId = BoxId(getObject("box_id", UUID::class.java).toString())
        val workoutId = WorkoutId(getObject("workout_id", UUID::class.java).toString())
        val version = getInt("version")
        val title = getString("title")
        val scheduledFor = getDate("scheduled_for").toLocalDate()
        val publishedAt = getObject("published_at", OffsetDateTime::class.java).toInstant()
        val publishedBy = CoachId(getString("published_by"))
        val document = mapper.readValue<WorkoutDocument>(getString("content_json"))
        check(document.schemaVersion == WorkoutDocument.CURRENT_SCHEMA_VERSION) {
            "Unsupported persisted publication schema"
        }
        check(document.title == title && document.version == version) {
            "Persisted publication metadata does not match its snapshot"
        }
        return WorkoutPublication(
            id = publicationId,
            workoutId = workoutId,
            boxId = boxId,
            version = version,
            title = title,
            scheduledFor = scheduledFor,
            publishedAt = publishedAt,
            publishedBy = publishedBy,
            blocks = document.blocks.map(WorkoutBlockDocument::toDomain),
        )
    }

    private fun uuid(value: String): UUID = UUID.fromString(value)
}
private data class WorkoutDocument(
    val schemaVersion: Int,
    val title: String,
    val version: Int,
    val blocks: List<WorkoutBlockDocument>,
) {
    companion object {
        fun from(title: String, version: Int, blocks: List<WorkoutBlock>) =
            WorkoutDocument(CURRENT_SCHEMA_VERSION, title, version, blocks.map(WorkoutBlockDocument::from))

        const val CURRENT_SCHEMA_VERSION = 1
    }
}
private data class WorkoutBlockDocument(
    val id: String,
    val title: String,
    val intent: String,
    val format: WorkoutFormatDocument?,
    val movements: List<MovementDocument>,
    val instructions: String?,
) {
    fun toDomain() = WorkoutBlock(
        id = WorkoutBlockId(id),
        title = title,
        intent = BlockIntent.valueOf(intent),
        format = format?.toDomain(),
        movements = movements.map(MovementDocument::toDomain),
        instructions = instructions,
    )

    companion object {
        fun from(block: WorkoutBlock) = WorkoutBlockDocument(
            id = block.id.value,
            title = block.title,
            intent = block.intent.name,
            format = block.format?.let(WorkoutFormatDocument::from),
            movements = block.movements.map(MovementDocument::from),
            instructions = block.instructions,
        )
    }
}

private data class WorkoutFormatDocument(
    val kind: String,
    val durationSeconds: Int? = null,
    val intervalSeconds: Int? = null,
    val intervalCount: Int? = null,
    val workSeconds: Int? = null,
    val restSeconds: Int? = null,
    val rounds: Int? = null,
    val sets: Int? = null,
    val timeCapSeconds: Int? = null,
) {
    fun toDomain(): WorkoutFormat = when (kind) {
        "AMRAP" -> WorkoutFormat.Amrap(requireNotNull(durationSeconds))
        "EMOM" -> WorkoutFormat.Emom(requireNotNull(intervalSeconds), requireNotNull(intervalCount))
        "FOR_TIME" -> WorkoutFormat.ForTime(timeCapSeconds)
        "TABATA" -> WorkoutFormat.Tabata(
            requireNotNull(workSeconds), requireNotNull(restSeconds), requireNotNull(rounds),
        )
        "INTERVALS" -> WorkoutFormat.Intervals(
            requireNotNull(rounds), requireNotNull(workSeconds), requireNotNull(restSeconds),
        )
        "ROUNDS_AND_REPS" -> WorkoutFormat.RoundsAndReps(rounds)
        "SETS_AND_REPS" -> WorkoutFormat.SetsAndReps(requireNotNull(sets), restSeconds)
        "CHIPPER" -> WorkoutFormat.Chipper(timeCapSeconds)
        else -> throw IllegalStateException("Unsupported persisted workout format")
    }

    companion object {
        fun from(format: WorkoutFormat): WorkoutFormatDocument = when (format) {
            is WorkoutFormat.Amrap -> WorkoutFormatDocument("AMRAP", durationSeconds = format.durationSeconds)
            is WorkoutFormat.Emom -> WorkoutFormatDocument(
                "EMOM", intervalSeconds = format.intervalSeconds, intervalCount = format.intervalCount,
            )
            is WorkoutFormat.ForTime -> WorkoutFormatDocument("FOR_TIME", timeCapSeconds = format.timeCapSeconds)
            is WorkoutFormat.Tabata -> WorkoutFormatDocument(
                "TABATA", workSeconds = format.workSeconds, restSeconds = format.restSeconds, rounds = format.rounds,
            )
            is WorkoutFormat.Intervals -> WorkoutFormatDocument(
                "INTERVALS", rounds = format.rounds, workSeconds = format.workSeconds, restSeconds = format.restSeconds,
            )
            is WorkoutFormat.RoundsAndReps -> WorkoutFormatDocument("ROUNDS_AND_REPS", rounds = format.rounds)
            is WorkoutFormat.SetsAndReps -> WorkoutFormatDocument(
                "SETS_AND_REPS", sets = format.sets, restSeconds = format.restSeconds,
            )
            is WorkoutFormat.Chipper -> WorkoutFormatDocument("CHIPPER", timeCapSeconds = format.timeCapSeconds)
        }
    }
}

private data class MovementDocument(
    val exerciseId: String,
    val movementName: String,
    val variants: Map<String, List<MovementTargetDocument>>,
) {
    fun toDomain() = MovementPrescription(
        exerciseId = ExerciseId(exerciseId),
        movementName = movementName,
        variants = variants.mapKeys { (variant, _) -> PrescriptionVariant.valueOf(variant) }
            .mapValues { (_, targets) -> targets.map(MovementTargetDocument::toDomain) },
    )

    companion object {
        fun from(movement: MovementPrescription) = MovementDocument(
            exerciseId = movement.exerciseId.value,
            movementName = movement.movementName,
            variants = movement.variants.mapKeys { (variant, _) -> variant.name }
                .mapValues { (_, targets) -> targets.map(MovementTargetDocument::from) },
        )
    }
}

private data class MovementTargetDocument(
    val kind: String,
    val value: String,
    val unit: String? = null,
) {
    fun toDomain(): MovementTarget = when (kind) {
        "REPETITIONS" -> MovementTarget.Repetitions(value.toInt())
        "LOAD" -> MovementTarget.Load(BigDecimal(value), MovementUnit.valueOf(requireNotNull(unit)))
        "DISTANCE" -> MovementTarget.Distance(BigDecimal(value), MovementUnit.valueOf(requireNotNull(unit)))
        "DURATION" -> MovementTarget.Duration(value.toInt())
        "CALORIES" -> MovementTarget.Calories(value.toInt())
        else -> throw IllegalStateException("Unsupported persisted movement target")
    }

    companion object {
        fun from(target: MovementTarget): MovementTargetDocument = when (target) {
            is MovementTarget.Repetitions -> MovementTargetDocument("REPETITIONS", target.count.toString())
            is MovementTarget.Load -> MovementTargetDocument("LOAD", target.amount.toPlainString(), target.unit.name)
            is MovementTarget.Distance -> MovementTargetDocument("DISTANCE", target.amount.toPlainString(), target.unit.name)
            is MovementTarget.Duration -> MovementTargetDocument("DURATION", target.seconds.toString())
            is MovementTarget.Calories -> MovementTargetDocument("CALORIES", target.count.toString())
        }
    }
}
