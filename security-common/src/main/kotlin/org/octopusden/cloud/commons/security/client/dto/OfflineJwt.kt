package org.octopusden.cloud.commons.security.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class OfflineJwt(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String
) {
    lateinit var refreshTokenExpDate: Instant
    lateinit var accessTokenExpDate: Instant

    @JsonProperty("refresh_expires_in")
    fun setRefreshTokenExpDate(expInSeconds: Long) {
        refreshTokenExpDate = getExpDate(expInSeconds)

    }

    @JsonProperty("expires_in")
    fun setAccessTokenExpDate(expInSeconds: Long) {
        accessTokenExpDate = getExpDate(expInSeconds)
    }

    private fun getExpDate(expInSeconds: Long) = if (expInSeconds == 0L) {
        Instant.MAX
    } else {
        Instant.now().plusSeconds(expInSeconds)
    }
}