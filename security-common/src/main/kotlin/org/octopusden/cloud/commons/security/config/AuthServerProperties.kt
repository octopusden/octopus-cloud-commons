package org.octopusden.cloud.commons.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("auth-server")
data class AuthServerProperties(
    var url: String? = null,
    var realm: String? = null
) {
    val openIdConfigurationUrl: String?
        get() = issuerUrl?.let { urlValue ->
            "$urlValue/.well-known/openid-configuration"
        }
    val issuerUrl: String?
        get() = url?.let { urlValue ->
            realm?.let { realmValue ->
                "$urlValue/realms/$realmValue"
            }
        }
}
