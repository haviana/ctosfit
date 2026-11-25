package com.ctosfit.identity.application.port.out

import com.ctosfit.identity.domain.BoxId
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId

/** Box-scoped membership operations used only after owner authorization. */
interface ManageMembershipsRepository {
    fun findAllByBox(boxId: BoxId): List<Membership>
    fun addOrReactivate(userId: UserId, boxId: BoxId, role: Role): Membership
    fun changeRole(userId: UserId, boxId: BoxId, role: Role): Membership?
    fun deactivate(userId: UserId, boxId: BoxId): Membership?
}
