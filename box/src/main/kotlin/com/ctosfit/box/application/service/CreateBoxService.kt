package com.ctosfit.box.application.service

import com.ctosfit.box.application.port.`in`.CreateBox
import com.ctosfit.box.application.port.out.AuthenticatedUserProvider
import com.ctosfit.box.application.port.out.BoxOnboardingRepository
import com.ctosfit.box.domain.BoxProfile

class CreateBoxService(
    private val userProvider: AuthenticatedUserProvider,
    private val repository: BoxOnboardingRepository,
) : CreateBox {
    override fun create(name: String, timeZone: String): BoxProfile {
        val box = BoxProfile.create(name, timeZone)
        return repository.createWithOwner(box, userProvider.currentUserId())
    }
}
