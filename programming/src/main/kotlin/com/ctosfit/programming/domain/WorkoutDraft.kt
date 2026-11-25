package com.ctosfit.programming.domain

import java.time.Instant
import java.time.LocalDate
import java.util.Collections

/** Editable workout. Every meaningful edit advances its version. */
class WorkoutDraft internal constructor(
    val id: WorkoutId,
    /** Must come from the application's resolved authenticated tenant context. */
    val boxId: BoxId,
    title: String,
    initialBlocks: List<WorkoutBlock> = emptyList(),
    initialVersion: Int = 1,
) {
    private val mutableBlocks = initialBlocks.map(WorkoutBlock::snapshotCopy).toMutableList()

    var title: String = title
        private set

    var version: Int = initialVersion
        private set

    val blocks: List<WorkoutBlock>
        get() = Collections.unmodifiableList(mutableBlocks.map(WorkoutBlock::snapshotCopy))

    init {
        require(title.isNotBlank()) { "Workout title must not be blank" }
        require(title.length <= 160) { "Workout title must be 160 characters or fewer" }
        require(initialVersion > 0) { "Workout version must be greater than zero" }
        require(mutableBlocks.map(WorkoutBlock::id).toSet().size == mutableBlocks.size) {
            "Workout block IDs must be unique"
        }
    }

    fun rename(newTitle: String) {
        require(newTitle.isNotBlank()) { "Workout title must not be blank" }
        require(newTitle.length <= 160) { "Workout title must be 160 characters or fewer" }
        if (title != newTitle) {
            title = newTitle
            advanceVersion()
        }
    }

    fun addBlock(block: WorkoutBlock, position: Int = mutableBlocks.size) {
        require(position in 0..mutableBlocks.size) { "Block position is out of range" }
        require(mutableBlocks.none { it.id == block.id }) { "Workout block IDs must be unique" }
        mutableBlocks.add(position, block.snapshotCopy())
        advanceVersion()
    }

    fun replaceBlock(blockId: WorkoutBlockId, replacement: WorkoutBlock) {
        val index = mutableBlocks.indexOfFirst { it.id == blockId }
        require(index >= 0) { "Workout block does not exist" }
        require(replacement.id == blockId) { "Replacement must preserve the block ID" }
        mutableBlocks[index] = replacement.snapshotCopy()
        advanceVersion()
    }

    fun moveBlock(blockId: WorkoutBlockId, newPosition: Int) {
        val oldPosition = mutableBlocks.indexOfFirst { it.id == blockId }
        require(oldPosition >= 0) { "Workout block does not exist" }
        require(newPosition in mutableBlocks.indices) { "Block position is out of range" }
        if (oldPosition != newPosition) {
            val block = mutableBlocks.removeAt(oldPosition)
            mutableBlocks.add(newPosition, block)
            advanceVersion()
        }
    }

    fun removeBlock(blockId: WorkoutBlockId) {
        val index = mutableBlocks.indexOfFirst { it.id == blockId }
        require(index >= 0) { "Workout block does not exist" }
        mutableBlocks.removeAt(index)
        advanceVersion()
    }

    fun publish(
        publicationId: PublicationId,
        scheduledFor: LocalDate,
        publishedAt: Instant,
        publishedBy: CoachId,
    ): WorkoutPublication {
        check(mutableBlocks.isNotEmpty()) { "A workout must contain at least one block before publication" }
        return WorkoutPublication(
            id = publicationId,
            workoutId = id,
            boxId = boxId,
            version = version,
            title = title,
            scheduledFor = scheduledFor,
            publishedAt = publishedAt,
            publishedBy = publishedBy,
            blocks = mutableBlocks,
        )
    }

    private fun advanceVersion() {
        version += 1
    }
}
