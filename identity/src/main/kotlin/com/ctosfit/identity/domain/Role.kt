package com.ctosfit.identity.domain

/**
 * Box membership roles understood by the application.
 *
 * Authentication providers must issue these exact values in a trusted roles claim.
 * Box membership and tenant scoping are resolved separately.
 */
enum class Role {
    BOX_OWNER,
    COACH,
    ATHLETE,
}
