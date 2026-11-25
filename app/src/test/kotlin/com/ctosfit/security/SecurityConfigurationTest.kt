package com.ctosfit.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigurationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun healthEndpointIsPublic() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }

    @Test
    fun anonymousRequestsToApiAreUnauthorized() {
        mockMvc.perform(get("/api/box-owner/probe"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun authenticatedApiDoesNotRequireGlobalRoleClaims() {
        mockMvc.perform(get("/api/box-owner/probe").with(jwt()))
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/api/coaches/probe").with(jwt()))
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/api/athletes/probe").with(jwt()))
            .andExpect(status().isNotFound)
    }
}
