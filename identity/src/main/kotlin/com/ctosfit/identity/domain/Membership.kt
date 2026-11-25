package com.ctosfit.identity.domain

/** Stable identity-side membership identifiers; the box module remains independently owned. */
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "User ID must not be blank" }
    }
}

@JvmInline
value class BoxId(val value: String) {
    init {
        require(value.isNotBlank()) { "Box ID must not be blank" }
    }
}

@JvmInline
value class MembershipId(val value: String) {
    init {
        require(value.isNotBlank()) { "Membership ID must not be blank" }
    }
}

enum class MembershipStatus {
    ACTIVE,
    INACTIVE,
}

/** A user's role applies only to the single box recorded on this membership. */
class Membership private constructor(
    val id: MembershipId,
    val userId: UserId,
    val boxId: BoxId,
    val role: Role,
    val status: MembershipStatus,
) {
    fun changeRole(newRole: Role): Membership {
        check(status == MembershipStatus.ACTIVE) { "An inactive membership cannot be changed" }
        return Membership(id, userId, boxId, newRole, status)
    }

    fun deactivate(): Membership =
        if (status == MembershipStatus.INACTIVE) this
        else Membership(id, userId, boxId, role, MembershipStatus.INACTIVE)

    companion object {
        fun active(id: MembershipId, userId: UserId, boxId: BoxId, role: Role): Membership =
            Membership(id, userId, boxId, role, MembershipStatus.ACTIVE)
    }
}
