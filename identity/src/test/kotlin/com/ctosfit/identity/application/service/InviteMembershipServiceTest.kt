package com.ctosfit.identity.application.service

import com.ctosfit.identity.application.port.out.CurrentMembershipAdministrator
import com.ctosfit.identity.application.port.out.ManageMembershipsRepository
import com.ctosfit.identity.application.port.out.MembershipAdministratorContext
import com.ctosfit.identity.application.port.out.MembershipInvitationDelivery
import com.ctosfit.identity.application.port.out.MembershipInvitationRepository
import com.ctosfit.identity.application.port.out.StoredMembershipInvitation
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.MembershipId
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InviteMembershipServiceTest {
    private val now = Instant.parse("2026-11-24T00:00:00Z")
    private val box = BoxId("box-1")
    private val owner = UserId("owner-1")

    @Test
    fun `owner invitation is hashed stored delivered and accepted once`() {
        var stored: StoredMembershipInvitation? = null
        var deliveredToken = ""
        var accepted = false
        val repository = object : MembershipInvitationRepository {
            override fun create(boxId: BoxId, email: String, role: Role, tokenHash: String, expiresAt: Instant, invitedBy: String) =
                StoredMembershipInvitation("inv-1", boxId, email, role, tokenHash, expiresAt, null).also { stored = it }
            override fun findPendingByTokenHash(tokenHash: String, current: Instant) = stored?.takeIf { it.tokenHash == tokenHash && it.expiresAt > current && !accepted }
            override fun markAccepted(id: String, acceptedAt: Instant): Boolean = !accepted.also { accepted = true }
        }
        val memberships = object : ManageMembershipsRepository {
            override fun findAllByBox(boxId: BoxId) = emptyList<Membership>()
            override fun addOrReactivate(userId: UserId, boxId: BoxId, role: Role) = Membership.active(MembershipId("m-1"), userId, boxId, role)
            override fun changeRole(userId: UserId, boxId: BoxId, role: Role) = null
            override fun deactivate(userId: UserId, boxId: BoxId) = null
        }
        val service = InviteMembershipService(
            CurrentMembershipAdministrator { MembershipAdministratorContext(owner, box, Role.BOX_OWNER) },
            repository,
            memberships,
            MembershipInvitationDelivery { _, _, token, _ -> deliveredToken = token },
            Clock.fixed(now, ZoneOffset.UTC),
            { "one-time-token" },
        )

        val invitation = service.invite(" Coach@Example.com ", Role.COACH)
        assertEquals("coach@example.com", invitation.email)
        assertEquals("one-time-token", deliveredToken)
        assertEquals(64, stored!!.tokenHash.length)
        assertEquals(Role.COACH, service.accept(deliveredToken, UserId("coach-1")).role)
        assertFailsWith<InvitationNotFoundException> { service.accept(deliveredToken, UserId("coach-1")) }
    }

    @Test
    fun `invitation cannot grant owner role`() {
        val service = InviteMembershipService(
            CurrentMembershipAdministrator { MembershipAdministratorContext(owner, box, Role.BOX_OWNER) },
            errorRepository(),
            errorMemberships(),
            MembershipInvitationDelivery { _, _, _, _ -> },
        )
        assertFailsWith<IllegalArgumentException> { service.invite("owner@example.com", Role.BOX_OWNER) }
    }

    private fun errorRepository() = object : MembershipInvitationRepository {
        override fun create(boxId: BoxId, email: String, role: Role, tokenHash: String, expiresAt: Instant, invitedBy: String) = error("not called")
        override fun findPendingByTokenHash(tokenHash: String, now: Instant): StoredMembershipInvitation? = null
        override fun markAccepted(id: String, acceptedAt: Instant) = false
    }

    private fun errorMemberships() = object : ManageMembershipsRepository {
        override fun findAllByBox(boxId: BoxId) = emptyList<Membership>()
        override fun addOrReactivate(userId: UserId, boxId: BoxId, role: Role) = error("not called")
        override fun changeRole(userId: UserId, boxId: BoxId, role: Role) = null
        override fun deactivate(userId: UserId, boxId: BoxId) = null
    }
}
