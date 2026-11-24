package com.ctosfit.box.application.port.`in`

import com.ctosfit.box.domain.BoxProfile

fun interface CreateBox {
    fun create(name: String, timeZone: String): BoxProfile
}
