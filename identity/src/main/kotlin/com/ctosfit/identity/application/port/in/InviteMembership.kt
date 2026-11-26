package com.ctosfit.identity.application.port.`in`

import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import java.time.Instant

data class MembershipInvitation(
    val id: String,
    val email: String,
    val role: Role,
    val expiresAt: Instant,
    val token: String,
)

interface InviteMembership {
    fun invite(email: String, role: Role): MembershipInvitation
    fun accept(token: String, userId: UserId): Membership
}
