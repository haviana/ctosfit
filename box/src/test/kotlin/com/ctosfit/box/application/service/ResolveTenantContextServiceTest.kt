package com.ctosfit.box.application.service

import com.ctosfit.box.application.port.out.ActiveMembershipLookup
import com.ctosfit.box.application.port.out.AuthenticatedUserProvider
import com.ctosfit.box.domain.AuthenticatedUserId
import com.ctosfit.box.domain.BoxId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolveTenantContextServiceTest {
    private val userId = AuthenticatedUserId("user-1")
    private val boxId = BoxId("box-1")

    @Test
    fun `resolves tenant context only for an active membership`() {
        val service = service { user, box -> user == userId && box == boxId }

        val context = service.resolve(boxId)

        assertEquals(boxId, context.boxId)
        assertEquals(userId, context.userId)
    }

    @Test
    fun `rejects a request-selected box without membership`() {
        val service = service { _, _ -> false }

        assertFailsWith<TenantAccessDeniedException> {
            service.resolve(boxId)
        }
    }

    @Test
    fun `rejects blank identifiers at the boundary`() {
        assertFailsWith<IllegalArgumentException> { BoxId(" ") }
        assertFailsWith<IllegalArgumentException> { AuthenticatedUserId("") }
    }

    private fun service(isActiveMember: (AuthenticatedUserId, BoxId) -> Boolean) =
        ResolveTenantContextService(
            AuthenticatedUserProvider { userId },
            ActiveMembershipLookup(isActiveMember),
        )
}
