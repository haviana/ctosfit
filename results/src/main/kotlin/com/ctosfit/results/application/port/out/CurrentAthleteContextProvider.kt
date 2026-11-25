package com.ctosfit.results.application.port.out

import com.ctosfit.results.domain.AthleteId
import com.ctosfit.results.domain.BoxId

/** Resolved from the authenticated user's active membership and athlete role. */
data class AthleteContext(
    val boxId: BoxId,
    val athleteId: AthleteId,
)

fun interface CurrentAthleteContextProvider {
    fun current(): AthleteContext
}
