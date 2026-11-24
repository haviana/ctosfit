package com.ctosfit.box.domain

@JvmInline
value class AuthenticatedUserId(val value: String) {
    init {
        require(value.isNotBlank()) { "Authenticated user ID must not be blank" }
    }
}
