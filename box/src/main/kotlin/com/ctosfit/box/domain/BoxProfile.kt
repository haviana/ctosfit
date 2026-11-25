package com.ctosfit.box.domain

import java.util.UUID
import java.time.ZoneId

class BoxProfile(val id: BoxId, val name: String, val timeZone: String) {
    init {
        require(name.isNotBlank() && name.length <= 120) { "Box name must be between 1 and 120 characters" }
        require(timeZone.isNotBlank() && timeZone.length <= 100) { "Time zone must be between 1 and 100 characters" }
        require(runCatching { ZoneId.of(timeZone) }.isSuccess) { "Time zone must be a valid IANA zone" }
    }

    companion object {
        fun create(name: String, timeZone: String) =
            BoxProfile(BoxId(UUID.randomUUID().toString()), name.trim(), timeZone.trim())
    }
}
