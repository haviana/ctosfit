package com.ctosfit.box.domain

/** Tenant scope established after membership authorization, never from a request value alone. */
class TenantContext private constructor(
    val boxId: BoxId,
    val userId: AuthenticatedUserId,
    val role: BoxRole,
) {
    fun requireRole(vararg allowedRoles: BoxRole) {
        check(role in allowedRoles) { "The authenticated user does not have a required role in this box" }
    }

    companion object {
        internal fun authorized(boxId: BoxId, userId: AuthenticatedUserId, role: BoxRole): TenantContext =
            TenantContext(boxId, userId, role)
    }
}
