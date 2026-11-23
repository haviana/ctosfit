package com.ctosfit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class CtosfitApplication

fun main(args: Array<String>) {
    runApplication<CtosfitApplication>(*args)
}

@RestController
class RootController {
    @GetMapping("/")
    fun status(): Map<String, String> = mapOf("service" to "ctosfit", "status" to "running")
}
