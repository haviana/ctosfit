package com.ctosfit.results.application.port.out

import com.ctosfit.results.domain.BoxId

/** Supplies a box after verifying the current user's active coach or owner membership. */
fun interface CurrentCoachBoxProvider {
    fun currentBoxId(): BoxId
}
