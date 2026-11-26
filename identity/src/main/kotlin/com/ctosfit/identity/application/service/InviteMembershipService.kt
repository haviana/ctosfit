package com.ctosfit.identity.application.service

import com.ctosfit.identity.application.port.`in`.InviteMembership
import com.ctosfit.identity.application.port.`in`.MembershipInvitation
import com.ctosfit.identity.application.port.out.CurrentMembershipAdministrator
import com.ctosfit.identity.application.port.out.ManageMembershipsRepository
import com.ctosfit.identity.application.port.out.MembershipInvitationDelivery
import com.ctosfit.identity.application.port.out.MembershipInvitationRepository
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64

class InviteMembershipService(
    private val administrator: CurrentMembershipAdministrator,
    private val invitations: MembershipInvitationRepository,
    private val memberships: ManageMembershipsRepository,
    private val delivery: MembershipInvitationDelivery,
    private val clock: Clock = Clock.systemUTC(),
    private val tokenGenerator: () -> String = ::newToken,
) : InviteMembership {
    override fun invite(email: String, role: Role): MembershipInvitation {
        val context = administrator.current()
        check(context.role == Role.BOX_OWNER) { "Only a box owner can invite members" }
        require(role != Role.BOX_OWNER) { "Invitations cannot grant the box owner role" }
        val normalizedEmail = email.trim().lowercase()
        require(normalizedEmail.contains('@')) { "A valid invitation email is required" }
        val token = tokenGenerator()
        val expiresAt = Instant.now(clock).plus(Duration.ofDays(7))
        val stored = invitations.create(
            BoxId(context.boxId.value), normalizedEmail, role, hash(token), expiresAt, context.userId.value,
        )
        delivery.deliver(normalizedEmail, role, token, expiresAt)
        return MembershipInvitation(stored.id, normalizedEmail, role, expiresAt, token)
    }

    override fun accept(token: String, userId: UserId): Membership {
        val now = Instant.now(clock)
        val invitation = invitations.findPendingByTokenHash(hash(token), now)
            ?: throw InvitationNotFoundException()
        check(invitations.markAccepted(invitation.id, now)) { "Invitation has already been accepted" }
        return memberships.addOrReactivate(userId, invitation.boxId, invitation.role)
    }

    private fun hash(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        private val random = SecureRandom()
        private fun newToken(): String {
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }
}

class InvitationNotFoundException : RuntimeException("Invitation is invalid, expired, or already accepted")
