package com.ctosfit.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfiguration {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().permitAll()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { }
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
    fun jwtDecoder(
        @Value("\${ctosfit.security.issuer-uri:}") issuerUri: String,
        @Value("\${ctosfit.security.jwk-set-uri:}") jwkSetUri: String,
        @Value("\${ctosfit.security.audience:ctosfit-api}") audience: String,
    ): JwtDecoder {
        if (issuerUri.isBlank() || jwkSetUri.isBlank()) {
            return JwtDecoder { throw BadJwtException("JWT issuer and JWK set URI must be configured") }
        }

        val audienceValidator = OAuth2TokenValidator<Jwt> { jwt ->
            if (audience in jwt.audience) OAuth2TokenValidatorResult.success()
            else OAuth2TokenValidatorResult.failure(
                OAuth2Error("invalid_token", "The token is not intended for this API", null),
            )
        }
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build().apply {
            setJwtValidator(
                DelegatingOAuth2TokenValidator(
                    JwtValidators.createDefaultWithIssuer(issuerUri),
                    audienceValidator,
                ),
            )
        }
    }
}
