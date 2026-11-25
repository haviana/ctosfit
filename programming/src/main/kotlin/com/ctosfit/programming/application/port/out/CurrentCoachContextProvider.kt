package com.ctosfit.programming.application.port.out

import com.ctosfit.programming.domain.BoxId
import com.ctosfit.programming.domain.CoachId

/** Box and coach are resolved together from the active authenticated membership. */
data class CoachContext(val boxId: BoxId, val coachId: CoachId)

fun interface CurrentCoachContextProvider {
    /** Must reject callers without an active coach membership in the selected box. */
    fun currentCoachContext(): CoachContext
}
