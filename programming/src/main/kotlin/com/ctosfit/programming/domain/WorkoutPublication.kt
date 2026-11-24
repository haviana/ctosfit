package com.ctosfit.programming.domain

import java.time.Instant
import java.time.LocalDate
import java.util.Collections

/** Immutable workout snapshot. It contains no reference to a mutable draft. */
class WorkoutPublication internal constructor(
    val id: PublicationId,
    val workoutId: WorkoutId,
    val boxId: BoxId,
    val version: Int,
    val title: String,
    val scheduledFor: LocalDate,
    val publishedAt: Instant,
    val publishedBy: CoachId,
    blocks: List<WorkoutBlock>,
) {
    val blocks: List<WorkoutBlock> = Collections.unmodifiableList(blocks.map(WorkoutBlock::snapshotCopy))
}
