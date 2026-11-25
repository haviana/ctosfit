package com.ctosfit.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ClientRegistrations
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

@Configuration
@ConditionalOnProperty(
    prefix = "ctosfit.security.oidc",
    name = ["enabled"],
    havingValue = "true",
)
class OidcClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun oidcClientRegistrationFactory(): OidcClientRegistrationFactory =
        OidcClientRegistrationFactory { issuerUri, clientId, clientSecret ->
            ClientRegistrations.fromIssuerLocation(issuerUri)
                .registrationId("ctosfit")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .clientName("Ctosfit")
                .build()
        }

    @Bean
    fun clientRegistrationRepository(
        @Value("\${ctosfit.security.oidc.issuer-uri}") issuerUri: String,
        @Value("\${ctosfit.security.oidc.client-id}") clientId: String,
        @Value("\${ctosfit.security.oidc.client-secret}") clientSecret: String,
        registrationFactory: OidcClientRegistrationFactory,
    ): ClientRegistrationRepository {
        check(issuerUri.isNotBlank() && clientId.isNotBlank() && clientSecret.isNotBlank()) {
            "OIDC sign-in requires a nonblank issuer URI, client ID, and client secret"
        }
        return InMemoryClientRegistrationRepository(
            registrationFactory.create(issuerUri, clientId, clientSecret),
        )
    }
}

fun interface OidcClientRegistrationFactory {
    fun create(issuerUri: String, clientId: String, clientSecret: String): ClientRegistration
}
