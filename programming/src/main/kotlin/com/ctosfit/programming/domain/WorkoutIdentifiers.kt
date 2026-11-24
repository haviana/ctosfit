package com.ctosfit.programming.domain

@JvmInline
value class WorkoutId(val value: String) {
    init {
        require(value.isNotBlank()) { "Workout ID must not be blank" }
    }
}

@JvmInline
value class BoxId(val value: String) {
    init {
        require(value.isNotBlank()) { "Box ID must not be blank" }
    }
}

@JvmInline
value class WorkoutBlockId(val value: String) {
    init {
        require(value.isNotBlank()) { "Workout block ID must not be blank" }
    }
}

@JvmInline
value class PublicationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Publication ID must not be blank" }
    }
}

@JvmInline
value class CoachId(val value: String) {
    init {
        require(value.isNotBlank()) { "Coach ID must not be blank" }
    }
}

@JvmInline
value class ExerciseId(val value: String) {
    init {
        require(value.isNotBlank()) { "Exercise ID must not be blank" }
    }
}
