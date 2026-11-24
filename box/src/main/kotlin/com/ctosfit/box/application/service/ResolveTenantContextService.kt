package com.ctosfit.box.application.service

import com.ctosfit.box.application.port.`in`.ResolveTenantContext
import com.ctosfit.box.application.port.out.AuthenticatedUserProvider
import com.ctosfit.box.application.port.out.ActiveMembershipLookup
import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.TenantContext

class ResolveTenantContextService(
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val activeMembershipLookup: ActiveMembershipLookup,
) : ResolveTenantContext {
    override fun resolve(selectedBoxId: BoxId): TenantContext {
        val userId = authenticatedUserProvider.currentUserId()
        if (!activeMembershipLookup.isActiveMember(userId, selectedBoxId)) {
            throw TenantAccessDeniedException()
        }
        return TenantContext.authorized(selectedBoxId, userId)
    }
}
