package com.ctosfit.box.domain

/** Tenant scope established after membership authorization, never from a request value alone. */
class TenantContext private constructor(
    val boxId: BoxId,
    val userId: AuthenticatedUserId,
) {
    companion object {
        internal fun authorized(boxId: BoxId, userId: AuthenticatedUserId): TenantContext =
            TenantContext(boxId, userId)
    }
}
