package com.ctosfit.security

import com.ctosfit.box.application.port.`in`.CreateBox
import com.ctosfit.box.domain.BoxProfile
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ExceptionHandler

data class CreateBoxRequest(
    @field:NotBlank @field:Size(max = 120) val name: String,
    @field:NotBlank @field:Size(max = 100) val timeZone: String = "UTC",
)

data class BoxResponse(val id: String, val name: String, val timeZone: String) {
    companion object { fun from(box: BoxProfile) = BoxResponse(box.id.value, box.name, box.timeZone) }
}

@RestController
@RequestMapping("/api/boxes")
class BoxController(private val createBox: CreateBox) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBoxRequest): BoxResponse =
        BoxResponse.from(createBox.create(request.name, request.timeZone))

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidBox(error: IllegalArgumentException): Map<String, String> =
        mapOf("error" to (error.message ?: "Invalid box details"))
}
