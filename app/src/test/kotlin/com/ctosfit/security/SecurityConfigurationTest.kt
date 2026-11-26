package com.ctosfit.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "ctosfit.security.oidc.enabled=true",
        "ctosfit.security.oidc.issuer-uri=https://issuer.example.test",
        "ctosfit.security.oidc.client-id=test-client",
        "ctosfit.security.oidc.client-secret=test-secret",
    ],
)
@Import(SecurityConfigurationTest.TestBeans::class, StubOidcRegistrationFactoryConfiguration::class)
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
    fun invalidBearerTokensAreUnauthorized() {
        mockMvc.perform(get("/api/box-owner/probe").header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun onlyBoxOwnersCanAccessOwnerRoutes() {
        mockMvc.perform(get("/api/box-owner/probe").with(jwt().authorities(SimpleGrantedAuthority("COACH"))))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/api/box-owner/probe").with(jwt().authorities(SimpleGrantedAuthority("BOX_OWNER"))))
            .andExpect(status().isNoContent)
    }

    @Test
    fun coachRoutesAllowOwnersAndCoachesOnly() {
        mockMvc.perform(get("/api/coaches/probe").with(jwt().authorities(SimpleGrantedAuthority("ATHLETE"))))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/api/coaches/probe").with(jwt().authorities(SimpleGrantedAuthority("COACH"))))
            .andExpect(status().isNoContent)
    }

    @Test
    fun athleteRoutesOnlyAllowAthletes() {
        mockMvc.perform(get("/api/athletes/probe").with(jwt().authorities(SimpleGrantedAuthority("COACH"))))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/api/athletes/probe").with(jwt().authorities(SimpleGrantedAuthority("ATHLETE"))))
            .andExpect(status().isNoContent)
    }

    @Test
    fun unmatchedApiRoutesAreDenied() {
        mockMvc.perform(get("/api/unclassified/probe").with(jwt().authorities(SimpleGrantedAuthority("BOX_OWNER"))))
            .andExpect(status().isForbidden)
    }

    @Test
    fun unknownRoleClaimsDoNotCreateAuthorities() {
        val token = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("user-1")
            .claim("roles", listOf("COACH", "ADMIN"))
            .build()

        val authorities = checkNotNull(SecurityConfiguration().jwtAuthenticationConverter().convert(token)).authorities
        assertEquals(
            setOf("COACH"),
            authorities.map { it.authority }.intersect(setOf("BOX_OWNER", "COACH", "ATHLETE")),
        )
        assertTrue(authorities.none { it.authority == "ADMIN" })
    }

    @Test
    fun oidcLoginStartsAuthorizationCodeFlow() {
        mockMvc.perform(get("/oauth2/authorization/ctosfit"))
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("https://issuer.example.test/authorize")))
    }

    @Test
    fun oidcRoleClaimsMapOnlyRecognizedRoles() {
        val now = Instant.now()
        val idToken = OidcIdToken(
            "id-token",
            now,
            now.plusSeconds(60),
            mapOf("sub" to "user-1", "roles" to listOf("ATHLETE", "ADMIN")),
        )
        val mapped = SecurityConfiguration().oidcRoleAuthoritiesMapper()
            .mapAuthorities(setOf(OidcUserAuthority(idToken)))

        assertEquals(setOf("ATHLETE"), mapped.map { it.authority }.intersect(setOf("BOX_OWNER", "COACH", "ATHLETE").toSet()))
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun protectedRouteProbe() = ProtectedRouteProbe()
    }

    @RestController
    class ProtectedRouteProbe {
        @GetMapping("/api/box-owner/probe")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        fun boxOwner() = Unit

        @GetMapping("/api/coaches/probe")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        fun coach() = Unit

        @GetMapping("/api/athletes/probe")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        fun athlete() = Unit
    }
}
