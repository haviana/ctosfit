package com.ctosfit.identity.application.service

import com.ctosfit.identity.application.port.`in`.FindActiveMembership
import com.ctosfit.identity.application.port.out.MembershipRepository
import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.MembershipStatus
import com.ctosfit.identity.domain.UserId

class FindActiveMembershipService(
    private val membershipRepository: MembershipRepository,
) : FindActiveMembership {
    override fun find(userId: UserId, boxId: BoxId): Membership? =
        membershipRepository.findByUserAndBox(userId, boxId)
            ?.takeIf {
                it.userId == userId && it.boxId == boxId && it.status == MembershipStatus.ACTIVE
            }
}
