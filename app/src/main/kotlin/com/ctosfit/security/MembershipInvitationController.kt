package com.ctosfit.security

import com.ctosfit.identity.application.port.`in`.InviteMembership
import com.ctosfit.identity.application.port.`in`.MembershipInvitation
import com.ctosfit.identity.application.service.InvitationNotFoundException
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class InvitationRequest(@field:Email @field:NotBlank val email: String, val role: Role)
data class InvitationResponse(val id: String, val email: String, val role: Role, val expiresAt: String, val token: String) {
    companion object { fun from(value: MembershipInvitation) = InvitationResponse(value.id, value.email, value.role, value.expiresAt.toString(), value.token) }
}
data class AcceptInvitationRequest(@field:NotBlank val token: String)

@RestController
@RequestMapping("/api")
class MembershipInvitationController(
    private val invitations: InviteMembership,
    private val authenticatedUserProvider: com.ctosfit.box.application.port.out.AuthenticatedUserProvider,
) {
    @PostMapping("/box-owner/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    fun invite(@Valid @RequestBody request: InvitationRequest): InvitationResponse =
        InvitationResponse.from(invitations.invite(request.email, request.role))

    @PostMapping("/invitations/accept")
    fun accept(@Valid @RequestBody request: AcceptInvitationRequest): Map<String, String> {
        val membership = invitations.accept(request.token, UserId(authenticatedUserProvider.currentUserId().value))
        return mapOf("membershipId" to membership.id.value, "boxId" to membership.boxId.value, "role" to membership.role.name)
    }

    @ExceptionHandler(InvitationNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun invalidInvitation(error: InvitationNotFoundException) = mapOf("error" to (error.message ?: "Invitation is invalid"))
}
