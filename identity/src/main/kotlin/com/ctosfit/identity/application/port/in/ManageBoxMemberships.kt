package com.ctosfit.identity.application.port.`in`

import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId

interface ManageBoxMemberships {
    fun list(): List<Membership>
    fun addOrReactivate(userId: UserId, role: Role): Membership
    fun changeRole(userId: UserId, role: Role): Membership
    fun deactivate(userId: UserId): Membership
}
