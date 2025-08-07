package org.octopusden.cloud.commons.security.client

import org.octopusden.cloud.commons.security.client.dto.OfflineJwt
import org.octopusden.cloud.commons.security.client.dto.OpenIdConfiguration
import org.octopusden.cloud.commons.security.client.dto.UserInfo
import org.octopusden.cloud.commons.security.config.AuthClientProperties
import org.octopusden.cloud.commons.security.config.AuthServerProperties
import org.octopusden.cloud.commons.security.utils.logger
import org.springframework.beans.factory.BeanInitializationException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

private const val PASSWORD_GRANT_TYPE = "password"
private const val REFRESH_TOKEN_GRANT_TYPE = "refresh_token"
private const val OFFLINE_ACCESS_SCOPE = "offline_access"
private val successStatuses = setOf(HttpStatus.OK)


@EnableConfigurationProperties(AuthClientProperties::class, AuthServerProperties::class)
@Component
class AuthServerClient(
    authServerProperties: AuthServerProperties,
    private val authClientProperties: AuthClientProperties
) {
    private val restTemplate = RestTemplate(SimpleClientHttpRequestFactory())
    private val openIdConfiguration: OpenIdConfiguration

    init {
        try {
            openIdConfiguration = authServerProperties.openIdConfigurationUrl
                ?.let { openIdConfigurationUrl ->
                    restTemplate.getForEntity(openIdConfigurationUrl, OpenIdConfiguration::class.java)
                        .body ?: throw OAuth2AuthenticationException(
                        OAuth2Error("invalid_request"),
                        "Cannot Extract openid-configuration via $openIdConfigurationUrl"
                    )
                } ?: throw BeanInitializationException("Open ID Configuration URL must be provided")

        } catch (e: Exception) {
            throw OAuth2AuthenticationException(
                OAuth2Error("invalid_request"),
                "Cannot get openid-configuration via ${authServerProperties.openIdConfigurationUrl}",
                e
            )
        }
    }

    fun getUserInfo(token: String): UserInfo {
        val headers = HttpHeaders()
        headers.add("Authorization", "Bearer $token")
        return validateResponse(
            restTemplate.exchange(
                openIdConfiguration.userInfoEndpoint,
                HttpMethod.GET,
                HttpEntity<String>(headers),
                UserInfo::class.java
            )
        )
    }

    fun generateOfflineJwt(username: String, password: String): OfflineJwt {
        log.trace("Generate Access Token for user: '$username'")
        return getOfflineJwt({
            with(it) {
                add("username", username)
                add("password", password)
            }
        }, PASSWORD_GRANT_TYPE, OFFLINE_ACCESS_SCOPE)
    }

    fun refreshOfflineJwt(refreshToken: String): OfflineJwt {
        log.trace("Refresh Token: $refreshToken")
        return getOfflineJwt({
            with(it) {
                add("refresh_token", refreshToken)
            }
        }, REFRESH_TOKEN_GRANT_TYPE)
    }

    private fun getOfflineJwt(
        extendParams: (LinkedMultiValueMap<String, String>) -> Unit = {},
        grantType: String,
        scope: String? = null
    ): OfflineJwt {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("client_id", authClientProperties.clientId)
            add("client_secret", authClientProperties.clientSecret)
            add("grant_type", grantType)
            scope?.let { scopeValue ->
                add("scope", scopeValue)
            }
        }
        extendParams.invoke(params)
        val headers = with(HttpHeaders()) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            this
        }
        val formEntity = HttpEntity(params, headers)
        val responseEntity = restTemplate.exchange(
            openIdConfiguration.tokenEndpoint,
            HttpMethod.POST,
            formEntity,
            OfflineJwt::class.java
        )
        return validateResponse(responseEntity)
    }

    companion object {
        private fun <T> validateResponse(responseEntity: ResponseEntity<T>): T {
            val statusCode = responseEntity.statusCode
            if (statusCode !in successStatuses) {
                throw OAuth2AuthenticationException(
                    OAuth2Error("invalid_auth_server_response"),
                    "Auth server error: '${responseEntity.body?.toString()}'"
                )
            }
            return responseEntity.body ?: throw IllegalStateException("Auth server response body is not accessible")
        }

        private val log = logger<AuthServerClient>()
    }
}
