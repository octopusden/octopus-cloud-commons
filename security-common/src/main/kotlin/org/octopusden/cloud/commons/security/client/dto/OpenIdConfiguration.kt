package org.octopusden.cloud.commons.security.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenIdConfiguration(
    @JsonProperty("userinfo_endpoint") val userInfoEndpoint: String,
    @JsonProperty("token_endpoint") val tokenEndpoint: String
)
