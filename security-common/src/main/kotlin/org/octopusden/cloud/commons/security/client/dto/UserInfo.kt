package org.octopusden.cloud.commons.security.client.dto

import com.fasterxml.jackson.annotation.JsonFormat

data class UserInfo(
    @field:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val roles: Collection<String>,

    @field:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val groups: Collection<String>
)
