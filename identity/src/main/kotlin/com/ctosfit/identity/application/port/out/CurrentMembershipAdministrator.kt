package com.ctosfit.identity.application.port.out

import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId

/** Authenticated user's trusted membership scope for membership administration. */
data class MembershipAdministratorContext(val userId: UserId, val boxId: BoxId, val role: Role)

fun interface CurrentMembershipAdministrator {
    fun current(): MembershipAdministratorContext
}
