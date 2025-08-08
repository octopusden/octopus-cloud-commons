package org.octopusden.cloud.commons.security.dto

data class Role(
    val name: String,
    val permissions: Set<String>
)
