package com.ctosfit.security

import com.ctosfit.identity.domain.Role
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper

@Configuration
class SecurityConfiguration {
    @Bean
    @Order(1)
    fun apiSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
    ): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/boxes").authenticated()
                it.requestMatchers("/api/box-owner/memberships", "/api/box-owner/memberships/{userId}").authenticated()
                it.requestMatchers("/api/box-owner/**").hasAuthority(Role.BOX_OWNER.name)
                it.requestMatchers("/api/coaches/**").hasAnyAuthority(Role.BOX_OWNER.name, Role.COACH.name)
                it.requestMatchers("/api/athletes/**").hasAuthority(Role.ATHLETE.name)
                it.anyRequest().denyAll()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
        return http.build()
    }

    @Bean
    @Order(2)
    fun webSecurityFilterChain(
        http: HttpSecurity,
        clientRegistrations: ObjectProvider<ClientRegistrationRepository>,
        oidcRoleAuthoritiesMapper: GrantedAuthoritiesMapper,
    ): SecurityFilterChain {
        http.authorizeHttpRequests {
            it.requestMatchers("/", "/actuator/health", "/actuator/health/**").permitAll()
            it.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            it.anyRequest().denyAll()
        }

        clientRegistrations.ifAvailable?.let {
            http.oauth2Login { login ->
                login.userInfoEndpoint { userInfo ->
                    userInfo.userAuthoritiesMapper(oidcRoleAuthoritiesMapper)
                }
            }
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(
        @Value("\${ctosfit.web.allowed-origins:http://localhost:5173}") allowedOrigins: String,
    ): CorsConfigurationSource {
        val cors = CorsConfiguration().apply {
            this.allowedOrigins = allowedOrigins.split(',').map(String::trim).filter(String::isNotEmpty)
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Box-Id")
            maxAge = 3_600L
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/api/**", cors) }
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val rolesConverter = Converter<Jwt, Collection<GrantedAuthority>> { jwt ->
            recognizedRoles(jwt.getClaimAsStringList("roles").orEmpty())
        }
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(rolesConverter)
        }
    }

    @Bean
    fun oidcRoleAuthoritiesMapper(): GrantedAuthoritiesMapper = GrantedAuthoritiesMapper { authorities ->
        val roles = authorities
            .filterIsInstance<OidcUserAuthority>()
            .flatMap { authority ->
                val claims = authority.idToken.claims
                val roleClaims = claims["roles"] as? Collection<*> ?: emptyList<Any>()
                roleClaims.filterIsInstance<String>()
            }
        authorities + recognizedRoles(roles)
    }

    @Bean
    fun jwtDecoder(
        @Value("\${ctosfit.security.issuer-uri:}") issuerUri: String,
        @Value("\${ctosfit.security.jwk-set-uri:}") jwkSetUri: String,
        @Value("\${ctosfit.security.audience:}") expectedAudience: String,
    ): JwtDecoder {
        if (issuerUri.isBlank() || jwkSetUri.isBlank() || expectedAudience.isBlank()) {
            return JwtDecoder { throw BadJwtException("JWT issuer, JWK set URI, and audience must be configured") }
        }

        val audienceValidator = OAuth2TokenValidator<Jwt> { token ->
            if (token.audience?.contains(expectedAudience) == true) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error("invalid_token", "JWT audience does not match this application", null),
                )
            }
        }

        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build().apply {
            setJwtValidator(
                org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator(
                    JwtValidators.createDefaultWithIssuer(issuerUri),
                    audienceValidator,
                ),
            )
        }
    }
}

private fun recognizedRoles(values: Collection<String>): Collection<GrantedAuthority> =
    values.mapNotNull { claim -> Role.entries.firstOrNull { it.name == claim } }
        .map { role -> SimpleGrantedAuthority(role.name) }
