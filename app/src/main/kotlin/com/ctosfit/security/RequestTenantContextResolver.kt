package com.ctosfit.security

import com.ctosfit.box.application.port.`in`.ResolveTenantContext
import com.ctosfit.box.domain.BoxId
import com.ctosfit.box.domain.BoxRole
import com.ctosfit.box.domain.TenantContext
import com.ctosfit.box.application.service.TenantAccessDeniedException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

/** Resolves the request's selected box through authenticated membership before use. */
@Component
class RequestTenantContextResolver(
    private val resolveTenantContext: ResolveTenantContext,
) {
    fun current(): TenantContext {
        val request = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw TenantAccessDeniedException()
        val rawBoxId = request.request.getHeader(SELECTED_BOX_HEADER)
            ?: throw SelectedBoxRequiredException()
        val boxId = runCatching { UUID.fromString(rawBoxId).toString() }
            .getOrElse { throw InvalidSelectedBoxException() }
        return try {
            resolveTenantContext.resolve(BoxId(boxId))
        } catch (_: TenantAccessDeniedException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "The authenticated user cannot access this box")
        }
    }

    fun currentCoach(): TenantContext = requireRoles(current(), BoxRole.BOX_OWNER, BoxRole.COACH)

    fun currentOwner(): TenantContext = requireRoles(current(), BoxRole.BOX_OWNER)

    fun currentAthlete(): TenantContext = requireRoles(current(), BoxRole.ATHLETE)

    private fun requireRoles(context: TenantContext, vararg roles: BoxRole): TenantContext {
        if (context.role !in roles) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Role is not allowed for this operation")
        }
        return context
    }

    private companion object {
        const val SELECTED_BOX_HEADER = "X-Box-Id"
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class SelectedBoxRequiredException : RuntimeException("X-Box-Id is required")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidSelectedBoxException : RuntimeException("X-Box-Id must be a UUID")
