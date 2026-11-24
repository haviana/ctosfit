package com.ctosfit.box.application.port.`in`

import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.TenantContext

/** Resolves a selected box for the already authenticated user. */
fun interface ResolveTenantContext {
    fun resolve(userId: AuthenticatedUserId, selectedBoxId: BoxId): TenantContext
}
