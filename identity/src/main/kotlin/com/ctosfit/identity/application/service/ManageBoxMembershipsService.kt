package com.ctosfit.identity.application.service

import com.ctosfit.identity.application.port.`in`.ManageBoxMemberships
import com.ctosfit.identity.application.port.out.CurrentMembershipAdministrator
import com.ctosfit.identity.application.port.out.ManageMembershipsRepository
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId

class ManageBoxMembershipsService(
    private val administrator: CurrentMembershipAdministrator,
    private val repository: ManageMembershipsRepository,
) : ManageBoxMemberships {
    override fun list(): List<Membership> {
        val context = requireOwner()
        return repository.findAllByBox(context.boxId)
    }

    override fun addOrReactivate(userId: UserId, role: Role): Membership {
        val context = requireOwner()
        requireOwnerRolePreserved(context.userId, userId, role)
        return repository.addOrReactivate(userId, context.boxId, role)
    }

    override fun changeRole(userId: UserId, role: Role): Membership {
        val context = requireOwner()
        requireOwnerRolePreserved(context.userId, userId, role)
        return repository.changeRole(userId, context.boxId, role)
            ?: throw MembershipNotFoundException()
    }

    override fun deactivate(userId: UserId): Membership {
        val context = requireOwner()
        require(userId != context.userId) { "An owner cannot deactivate their own membership" }
        return repository.deactivate(userId, context.boxId)
            ?: throw MembershipNotFoundException()
    }

    private fun requireOwnerRolePreserved(actor: UserId, userId: UserId, role: Role) {
        require(userId != actor || role == Role.BOX_OWNER) {
            "An owner cannot remove their own owner role"
        }
    }

    private fun requireOwner() = administrator.current().also {
        check(it.role == Role.BOX_OWNER) { "Only a box owner can manage memberships" }
    }
}

class MembershipNotFoundException : RuntimeException("Membership was not found in this box")
