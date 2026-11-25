package com.ctosfit.results.domain

@JvmInline
value class ResultId(val value: String) {
    init {
        require(value.isNotBlank()) { "Result ID must not be blank" }
    }
}

@JvmInline
value class BoxId(val value: String) {
    init {
        require(value.isNotBlank()) { "Box ID must not be blank" }
    }
}

@JvmInline
value class AthleteId(val value: String) {
    init {
        require(value.isNotBlank()) { "Athlete ID must not be blank" }
    }
}

@JvmInline
value class PublicationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Publication ID must not be blank" }
    }
}

@JvmInline
value class MetricId(val value: String) {
    init {
        require(value.isNotBlank()) { "Metric ID must not be blank" }
    }
}

@JvmInline
value class PrescriptionId(val value: String) {
    init {
        require(value.isNotBlank()) { "Prescription ID must not be blank" }
    }
}
