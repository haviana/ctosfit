package com.ctosfit.exercise.application.port.out

import com.ctosfit.exercise.domain.TenantId

/** Supplies tenant scope from trusted authenticated membership context, not request data. */
fun interface CurrentTenantProvider {
    fun currentTenantId(): TenantId
}
