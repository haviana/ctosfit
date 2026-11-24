package com.ctosfit.box.application.port.out

import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxId

/** Implemented at the composition boundary using the identity module's membership capability. */
fun interface ActiveMembershipLookup {
    fun isActiveMember(userId: AuthenticatedUserId, boxId: BoxId): Boolean
}
