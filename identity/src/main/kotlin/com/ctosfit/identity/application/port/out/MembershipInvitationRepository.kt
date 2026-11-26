package com.ctosfit.identity.application.port.out

import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Role
import java.time.Instant

data class StoredMembershipInvitation(
    val id: String,
    val boxId: BoxId,
    val email: String,
    val role: Role,
    val tokenHash: String,
    val expiresAt: Instant,
    val acceptedAt: Instant?,
)

interface MembershipInvitationRepository {
    fun create(boxId: BoxId, email: String, role: Role, tokenHash: String, expiresAt: Instant, invitedBy: String): StoredMembershipInvitation
    fun findPendingByTokenHash(tokenHash: String, now: Instant): StoredMembershipInvitation?
    fun markAccepted(id: String, acceptedAt: Instant): Boolean
}
