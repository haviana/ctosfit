package com.ctosfit.programming.application.port.out

import com.ctosfit.programming.domain.BoxId

/** Supplies a box resolved from the authenticated user's active membership. */
fun interface CurrentBoxContextProvider {
    fun currentBoxId(): BoxId
}
