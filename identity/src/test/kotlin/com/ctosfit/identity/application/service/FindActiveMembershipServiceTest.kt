package com.ctosfit.identity.application.service

import com.ctosfit.identity.application.port.out.MembershipRepository
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.MembershipId
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FindActiveMembershipServiceTest {
    private val userId = UserId("user-1")
    private val boxId = BoxId("box-1")

    @Test
    fun `returns role only for matching active user and box membership`() {
        val member = Membership.active(MembershipId("membership-1"), userId, boxId, Role.COACH)
        val lookup = FindActiveMembershipService(MembershipRepository { user, box ->
            member.takeIf { user == member.userId && box == member.boxId }
        })

        assertEquals(Role.COACH, lookup.find(userId, boxId)?.role)
        assertNull(lookup.find(userId, BoxId("other-box")))
        assertNull(lookup.find(UserId("other-user"), boxId))
    }

    @Test
    fun `does not return inactive memberships`() {
        val inactive = Membership.active(MembershipId("membership-1"), userId, boxId, Role.ATHLETE).deactivate()
        val lookup = FindActiveMembershipService(MembershipRepository { _, _ -> inactive })

        assertNull(lookup.find(userId, boxId))
    }

    @Test
    fun `rejects a repository result for a different tenant or user`() {
        val unrelated = Membership.active(
            MembershipId("membership-other"),
            UserId("someone-else"),
            BoxId("another-box"),
            Role.BOX_OWNER,
        )
        val lookup = FindActiveMembershipService(MembershipRepository { _, _ -> unrelated })

        assertNull(lookup.find(userId, boxId))
    }
}
