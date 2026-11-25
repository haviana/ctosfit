package com.ctosfit.box.application.port.`in`

import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.TenantContext

/** Resolves a selected box for the current authenticated user. */
fun interface ResolveTenantContext {
    fun resolve(selectedBoxId: BoxId): TenantContext
}
