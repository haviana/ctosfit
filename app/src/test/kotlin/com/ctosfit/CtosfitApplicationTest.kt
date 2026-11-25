package com.ctosfit

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CtosfitApplicationTest {
    @Test
    fun `application context starts`() {
        // Successful context load verifies the composition root and module classpath.
    }
}
