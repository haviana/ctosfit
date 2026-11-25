package com.ctosfit.box.application.port.out

import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxProfile

/** Atomically persists the box and its initial BOX_OWNER membership. */
fun interface BoxOnboardingRepository {
    fun createWithOwner(box: BoxProfile, ownerId: AuthenticatedUserId): BoxProfile
}
