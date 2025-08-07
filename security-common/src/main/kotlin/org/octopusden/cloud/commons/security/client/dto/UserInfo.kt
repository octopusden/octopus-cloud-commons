package org.octopusden.cloud.commons.security.client.dto

data class UserInfo(
    val roles: Collection<String>,
    val groups: Collection<String>
)
