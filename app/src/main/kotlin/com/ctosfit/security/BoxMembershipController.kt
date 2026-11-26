package com.ctosfit.security

import com.ctosfit.identity.application.port.`in`.ManageBoxMemberships
import com.ctosfit.identity.application.service.MembershipNotFoundException
import com.ctosfit.identity.domain.Membership
import com.ctosfit.identity.domain.Role
import com.ctosfit.identity.domain.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class MembershipRequest(
    @field:NotBlank @field:Size(max = 255) val userId: String,
    val role: Role,
)

data class MembershipRoleRequest(val role: Role)

data class MembershipResponse(
    val id: String,
    val userId: String,
    val boxId: String,
    val role: Role,
    val status: String,
) {
    companion object {
        fun from(membership: Membership) = MembershipResponse(
            membership.id.value,
            membership.userId.value,
            membership.boxId.value,
            membership.role,
            membership.status.name,
        )
    }
}

@RestController
@RequestMapping("/api/box-owner/memberships")
class BoxMembershipController(private val memberships: ManageBoxMemberships) {
    @GetMapping
    fun list(): List<MembershipResponse> = memberships.list().map(MembershipResponse::from)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addOrReactivate(@Valid @RequestBody request: MembershipRequest): MembershipResponse =
        MembershipResponse.from(memberships.addOrReactivate(UserId(request.userId.trim()), request.role))

    @PatchMapping("/{userId}")
    fun changeRole(
        @PathVariable userId: String,
        @Valid @RequestBody request: MembershipRoleRequest,
    ): MembershipResponse =
        MembershipResponse.from(memberships.changeRole(UserId(userId), request.role))

    @DeleteMapping("/{userId}")
    fun deactivate(@PathVariable userId: String): MembershipResponse =
        MembershipResponse.from(memberships.deactivate(UserId(userId)))

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun invariantConflict(error: IllegalStateException): Map<String, String> =
        mapOf("error" to (error.message ?: "Membership change conflicts with a box invariant"))

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun forbidden(error: IllegalArgumentException): Map<String, String> =
        mapOf("error" to (error.message ?: "Membership change is not allowed"))

    @ExceptionHandler(MembershipNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFound(error: MembershipNotFoundException): Map<String, String> =
        mapOf("error" to (error.message ?: "Membership was not found in this box"))
}
