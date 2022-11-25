package org.octopusden.cloud.commons.security.dto

data class User(val username: String, val roles: Collection<Role>, val groups: Collection<String>)
