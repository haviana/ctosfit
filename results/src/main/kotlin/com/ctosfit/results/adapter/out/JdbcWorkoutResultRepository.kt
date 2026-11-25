package com.ctosfit.results.adapter.out

import com.ctosfit.results.application.port.out.WorkoutResultRepository
import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId
import com.ctosfit.results.domain.CaloriesScore
import com.ctosfit.results.domain.CompletionScore
import com.ctosfit.results.domain.CompletionStatus
import com.ctosfit.results.domain.DistanceScore
import com.ctosfit.results.domain.DistanceUnit
import com.ctosfit.results.domain.LoadScore
import com.ctosfit.results.domain.LoadUnit
import com.ctosfit.results.domain.MetricId
import com.ctosfit.results.domain.PrescriptionId
import com.ctosfit.results.domain.PublicationId
import com.ctosfit.results.domain.ResultId
import com.ctosfit.results.domain.RoundsAndRepsScore
import com.ctosfit.results.domain.TimeOutcome
import com.ctosfit.results.domain.TimeScore
import com.ctosfit.results.domain.WorkoutResult
import com.ctosfit.results.domain.WorkoutScore
import com.ctosfit.results.domain.RepsScore
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types
import java.time.Duration
import java.util.UUID
import javax.sql.DataSource

/** PostgreSQL adapter for append-only typed results and athlete-scoped history. */
class JdbcWorkoutResultRepository(
    private val dataSource: DataSource,
) : WorkoutResultRepository {
    override fun save(result: WorkoutResult): WorkoutResult {
        val score = scoreColumns(result.score)
        val sql = """
            INSERT INTO workout_results (
                id, box_id, athlete_id, publication_id, metric_id, prescription_id, submitted_at,
                score_type, load_amount, load_unit, elapsed_seconds, elapsed_nanos, time_outcome,
                integer_value, rounds, additional_reps, distance_amount, distance_unit, completion_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, uuid(result.id.value, "result ID"))
                statement.setObject(2, uuid(result.boxId.value, "box ID"))
                statement.setString(3, result.athleteId.value)
                statement.setObject(4, uuid(result.publicationId.value, "publication ID"))
                statement.setObject(5, uuid(result.metricId.value, "metric ID"))
                statement.setString(6, result.prescriptionId.value)
                statement.setTimestamp(7, Timestamp.from(result.submittedAt))
                statement.setString(8, score.type)
                statement.setNullableBigDecimal(9, score.loadAmount)
                statement.setNullableString(10, score.loadUnit)
                statement.setNullableLong(11, score.elapsedSeconds)
                statement.setNullableInt(12, score.elapsedNanos)
                statement.setNullableString(13, score.timeOutcome)
                statement.setNullableInt(14, score.integerValue)
                statement.setNullableInt(15, score.rounds)
                statement.setNullableInt(16, score.additionalReps)
                statement.setNullableBigDecimal(17, score.distanceAmount)
                statement.setNullableString(18, score.distanceUnit)
                statement.setNullableString(19, score.completionStatus)
                statement.executeUpdate()
            }
        }
        return result
    }

    override fun findByBoxAndAthlete(boxId: BoxId, athleteId: AthleteId): List<WorkoutResult> {
        val sql = """
            SELECT id, box_id, athlete_id, publication_id, metric_id, prescription_id, submitted_at,
                   score_type, load_amount, load_unit, elapsed_seconds, elapsed_nanos, time_outcome,
                   integer_value, rounds, additional_reps, distance_amount, distance_unit, completion_status
            FROM workout_results
            WHERE box_id = ? AND athlete_id = ?
            ORDER BY submitted_at DESC, id DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, uuid(boxId.value, "box ID"))
                statement.setString(2, athleteId.value)
                statement.executeQuery().use { results ->
                    buildList {
                        while (results.next()) add(results.toWorkoutResult())
                    }
                }
            }
        }
    }

    override fun findByBox(boxId: BoxId): List<WorkoutResult> {
        val sql = """
            SELECT id, box_id, athlete_id, publication_id, metric_id, prescription_id, submitted_at,
                   score_type, load_amount, load_unit, elapsed_seconds, elapsed_nanos, time_outcome,
                   integer_value, rounds, additional_reps, distance_amount, distance_unit, completion_status
            FROM workout_results
            WHERE box_id = ?
            ORDER BY submitted_at DESC, id DESC
        """.trimIndent()

        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setObject(1, uuid(boxId.value, "box ID"))
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) add(rows.toWorkoutResult())
                    }
                }
            }
        }
    }

    private fun ResultSet.toWorkoutResult(): WorkoutResult = WorkoutResult(
        id = ResultId(getObject("id", UUID::class.java).toString()),
        boxId = BoxId(getObject("box_id", UUID::class.java).toString()),
        athleteId = AthleteId(getString("athlete_id")),
        publicationId = PublicationId(getObject("publication_id", UUID::class.java).toString()),
        metricId = MetricId(getString("metric_id")),
        prescriptionId = PrescriptionId(getString("prescription_id")),
        submittedAt = getTimestamp("submitted_at").toInstant(),
        score = toScore(),
    )

    private fun ResultSet.toScore(): WorkoutScore = when (getString("score_type")) {
        "LOAD" -> LoadScore(getBigDecimal("load_amount"), enumValue<LoadUnit>("load_unit"))
        "TIME" -> TimeScore(
            elapsed = Duration.ofSeconds(requiredLong("elapsed_seconds"), requiredInt("elapsed_nanos").toLong()),
            outcome = enumValue("time_outcome"),
        )
        "REPS" -> RepsScore(requiredInt("integer_value"))
        "ROUNDS_AND_REPS" -> RoundsAndRepsScore(
            rounds = requiredInt("rounds"),
            additionalReps = requiredInt("additional_reps"),
        )
        "DISTANCE" -> DistanceScore(getBigDecimal("distance_amount"), enumValue("distance_unit"))
        "CALORIES" -> CaloriesScore(requiredInt("integer_value"))
        "COMPLETION" -> CompletionScore(enumValue("completion_status"))
        else -> throw IllegalStateException("Unrecognized persisted workout score type")
    }

    private inline fun <reified T : Enum<T>> ResultSet.enumValue(column: String): T =
        enumValues<T>().firstOrNull { it.name == getString(column) }
            ?: throw IllegalStateException("Unrecognized persisted workout score value")

    private fun ResultSet.requiredInt(column: String): Int {
        val value = getInt(column)
        check(!wasNull()) { "Persisted workout score is missing $column" }
        return value
    }

    private fun ResultSet.requiredLong(column: String): Long {
        val value = getLong(column)
        check(!wasNull()) { "Persisted workout score is missing $column" }
        return value
    }

    private fun scoreColumns(score: WorkoutScore): ScoreColumns = when (score) {
        is LoadScore -> ScoreColumns(type = "LOAD", loadAmount = score.amount, loadUnit = score.unit.name)
        is TimeScore -> ScoreColumns(
            type = "TIME",
            elapsedSeconds = score.elapsed.seconds,
            elapsedNanos = score.elapsed.nano,
            timeOutcome = score.outcome.name,
        )
        is RepsScore -> ScoreColumns(type = "REPS", integerValue = score.reps)
        is RoundsAndRepsScore -> ScoreColumns(
            type = "ROUNDS_AND_REPS",
            rounds = score.rounds,
            additionalReps = score.additionalReps,
        )
        is DistanceScore -> ScoreColumns(
            type = "DISTANCE",
            distanceAmount = score.amount,
            distanceUnit = score.unit.name,
        )
        is CaloriesScore -> ScoreColumns(type = "CALORIES", integerValue = score.calories)
        is CompletionScore -> ScoreColumns(type = "COMPLETION", completionStatus = score.status.name)
    }

    private fun uuid(value: String, label: String): UUID =
        runCatching { UUID.fromString(value) }
            .getOrElse { throw IllegalArgumentException("Persisted $label must be a UUID", it) }

    private fun PreparedStatement.setNullableString(index: Int, value: String?) {
        if (value == null) setNull(index, Types.VARCHAR) else setString(index, value)
    }

    private fun PreparedStatement.setNullableInt(index: Int, value: Int?) {
        if (value == null) setNull(index, Types.INTEGER) else setInt(index, value)
    }

    private fun PreparedStatement.setNullableLong(index: Int, value: Long?) {
        if (value == null) setNull(index, Types.BIGINT) else setLong(index, value)
    }

    private fun PreparedStatement.setNullableBigDecimal(index: Int, value: java.math.BigDecimal?) {
        if (value == null) setNull(index, Types.NUMERIC) else setBigDecimal(index, value)
    }

    private data class ScoreColumns(
        val type: String,
        val loadAmount: java.math.BigDecimal? = null,
        val loadUnit: String? = null,
        val elapsedSeconds: Long? = null,
        val elapsedNanos: Int? = null,
        val timeOutcome: String? = null,
        val integerValue: Int? = null,
        val rounds: Int? = null,
        val additionalReps: Int? = null,
        val distanceAmount: java.math.BigDecimal? = null,
        val distanceUnit: String? = null,
        val completionStatus: String? = null,
    )
}

