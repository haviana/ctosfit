package com.ctosfit.programming.application.port.out

import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.WorkoutDraft
import com.ctosfit.programming.domain.WorkoutId

/** Every draft operation is scoped by box before the identifier is resolved. */
interface WorkoutDraftRepository {
    fun findByBoxAndId(boxId: BoxId, workoutId: WorkoutId): WorkoutDraft?

    fun save(draft: WorkoutDraft): WorkoutDraft
}
