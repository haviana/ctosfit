package com.ctosfit.box.application.port.out

import com.ctosfit.box.domain.AuthenticatedUserId

/** Supplies identity from trusted authentication context, never from request data. */
fun interface AuthenticatedUserProvider {
    fun currentUserId(): AuthenticatedUserId
}
