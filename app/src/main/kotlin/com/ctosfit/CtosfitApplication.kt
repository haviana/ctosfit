package com.ctosfit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.ctosfit"])
class CtosfitApplication

fun main(args: Array<String>) {
    runApplication<CtosfitApplication>(*args)
}
