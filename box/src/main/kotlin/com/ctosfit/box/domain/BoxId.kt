package com.ctosfit.box.domain

@JvmInline
value class BoxId(val value: String) {
    init {
        require(value.isNotBlank()) { "Box ID must not be blank" }
    }
}
