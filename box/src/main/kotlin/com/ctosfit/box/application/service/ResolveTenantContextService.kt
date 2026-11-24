package com.ctosfit.box.application.service

import com.ctosfit.box.application.port.`in`.ResolveTenantContext
import com.ctosfit.box.application.port.out.ActiveMembershipLookup
import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.TenantContext

class ResolveTenantContextService(
    private val activeMembershipLookup: ActiveMembershipLookup,
) : ResolveTenantContext {
    override fun resolve(userId: AuthenticatedUserId, selectedBoxId: BoxId): TenantContext {
        if (!activeMembershipLookup.isActiveMember(userId, selectedBoxId)) {
            throw TenantAccessDeniedException()
        }
        return TenantContext.authorized(selectedBoxId, userId)
    }
}
