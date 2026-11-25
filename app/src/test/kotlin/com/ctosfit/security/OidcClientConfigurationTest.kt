package com.ctosfit.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

class OidcClientConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(OidcClientConfiguration::class.java)

    @Test
    fun oidcRegistrationIsDisabledByDefault() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(ClientRegistrationRepository::class.java)
        }
    }

    @Test
    fun enabledConfigurationBuildsClientRegistrationFromConfiguredProperties() {
        contextRunner
            .withUserConfiguration(StubOidcRegistrationFactoryConfiguration::class.java)
            .withPropertyValues(
                "ctosfit.security.oidc.enabled=true",
                "ctosfit.security.oidc.issuer-uri=https://issuer.example.test",
                "ctosfit.security.oidc.client-id=test-client",
                "ctosfit.security.oidc.client-secret=test-secret",
            )
            .run { context ->
                assertThat(context.startupFailure).isNull()
                val repository = context.getBean(ClientRegistrationRepository::class.java)
                val registration = checkNotNull(repository.findByRegistrationId("ctosfit"))
                assertThat(registration.clientId).isEqualTo("test-client")
                assertThat(registration.clientSettings.isRequireProofKey).isTrue()
            }
    }

    @Test
    fun enabledConfigurationRejectsMissingCredentials() {
        contextRunner
            .withPropertyValues(
                "ctosfit.security.oidc.enabled=true",
                "ctosfit.security.oidc.issuer-uri=",
                "ctosfit.security.oidc.client-id=",
                "ctosfit.security.oidc.client-secret=",
            )
            .run { context ->
                assertThat(context.startupFailure).isNotNull()
            }
    }
}

@TestConfiguration
class StubOidcRegistrationFactoryConfiguration {
    @Bean
    @Primary
    fun testOidcClientRegistrationFactory(): OidcClientRegistrationFactory =
        OidcClientRegistrationFactory { issuerUri, clientId, clientSecret ->
            ClientRegistration.withRegistrationId("ctosfit")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("$issuerUri/authorize")
                .tokenUri("$issuerUri/token")
                .userInfoUri("$issuerUri/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("$issuerUri/jwks")
                .issuerUri(issuerUri)
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .clientName("Test identity provider")
                .build()
        }
}
