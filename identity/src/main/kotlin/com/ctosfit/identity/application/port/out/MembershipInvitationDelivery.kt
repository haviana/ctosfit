package com.ctosfit.identity.application.port.out

import com.ctosfit.identity.domain.Role
import java.time.Instant

fun interface MembershipInvitationDelivery {
    fun deliver(email: String, role: Role, token: String, expiresAt: Instant)
}
