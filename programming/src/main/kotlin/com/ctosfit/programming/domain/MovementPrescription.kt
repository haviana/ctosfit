package com.ctosfit.programming.domain

import java.math.BigDecimal
import java.util.Collections

enum class MovementUnit {
    KILOGRAMS,
    POUNDS,
    METERS,
    KILOMETERS,
    CALORIES,
}

enum class PrescriptionVariant {
    RX,
    SCALED,
    BEGINNER,
}

sealed interface MovementTarget {
    data class Repetitions(val count: Int) : MovementTarget {
        init {
            require(count > 0) { "Repetitions must be greater than zero" }
        }
    }

    data class Load(val amount: BigDecimal, val unit: MovementUnit) : MovementTarget {
        init {
            require(amount > BigDecimal.ZERO) { "Load must be greater than zero" }
            require(unit == MovementUnit.KILOGRAMS || unit == MovementUnit.POUNDS) {
                "Load unit must be kilograms or pounds"
            }
        }
    }

    data class Distance(val amount: BigDecimal, val unit: MovementUnit) : MovementTarget {
        init {
            require(amount > BigDecimal.ZERO) { "Distance must be greater than zero" }
            require(unit == MovementUnit.METERS || unit == MovementUnit.KILOMETERS) {
                "Distance unit must be meters or kilometers"
            }
        }
    }

    data class Duration(val seconds: Int) : MovementTarget {
        init {
            require(seconds > 0) { "Duration must be greater than zero" }
        }
    }

    data class Calories(val count: Int) : MovementTarget {
        init {
            require(count > 0) { "Calories must be greater than zero" }
        }
    }
}

class MovementPrescription(
    val exerciseId: ExerciseId,
    val movementName: String,
    variants: Map<PrescriptionVariant, List<MovementTarget>>,
) {
    val variants: Map<PrescriptionVariant, List<MovementTarget>> =
        Collections.unmodifiableMap(
            variants.mapValues { (_, targets) -> Collections.unmodifiableList(targets.toList()) },
        )

    fun targetsFor(variant: PrescriptionVariant): List<MovementTarget> =
        variants[variant] ?: emptyList()

    init {
        require(movementName.isNotBlank()) { "Movement name must not be blank" }
        require(this.variants.isNotEmpty()) { "A movement must have at least one prescription variant" }
        require(this.variants.values.all { targets -> targets.isNotEmpty() }) {
            "Each prescription variant must have at least one target"
        }
    }

    internal fun snapshotCopy(): MovementPrescription =
        MovementPrescription(exerciseId, movementName, variants)
}
