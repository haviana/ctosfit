package com.ctosfit.identity.application.port.out

import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.UserId

/** Membership reads are keyed by both user and box to preserve tenant scope. */
fun interface MembershipRepository {
    fun findByUserAndBox(userId: UserId, boxId: BoxId): Membership?
}
