package org.octopusden.cloud.commons.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.security.oauth2.client.registration.octopus-keycloak")
data class AuthClientProperties(var clientId: String? = null, var clientSecret: String? = null)
