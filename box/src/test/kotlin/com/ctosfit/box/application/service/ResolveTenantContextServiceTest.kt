package com.ctosfit.box.application.service

import com.ctosfit.box.application.port.out.ActiveMembershipLookup
import com.ctosfit.box.application.port.out.AuthenticatedUserProvider
import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.BoxRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolveTenantContextServiceTest {
    private val userId = AuthenticatedUserId("user-1")
    private val boxId = BoxId("box-1")

    @Test
    fun `resolves tenant context only for an active membership`() {
        val service = service { user, box ->
            if (user == userId && box == boxId) BoxRole.COACH else null
        }

        val context = service.resolve(boxId)

        assertEquals(boxId, context.boxId)
        assertEquals(userId, context.userId)
        assertEquals(BoxRole.COACH, context.role)
    }

    @Test
    fun `rejects a request-selected box without membership`() {
        val service = service { _, _ -> null }

        assertFailsWith<TenantAccessDeniedException> {
            service.resolve(boxId)
        }
    }

    @Test
    fun `rejects blank identifiers at the boundary`() {
        assertFailsWith<IllegalArgumentException> { BoxId(" ") }
        assertFailsWith<IllegalArgumentException> { AuthenticatedUserId("") }
    }

    private fun service(findActiveRole: (AuthenticatedUserId, BoxId) -> BoxRole?) =
        ResolveTenantContextService(
            AuthenticatedUserProvider { userId },
            ActiveMembershipLookup(findActiveRole),
        )
}
