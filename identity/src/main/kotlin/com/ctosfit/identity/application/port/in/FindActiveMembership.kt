package com.ctosfit.identity.application.port.`in`

import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.UserId

/** Finds membership for the authenticated user in one explicitly selected box. */
fun interface FindActiveMembership {
    fun find(userId: UserId, boxId: BoxId): Membership?
}
