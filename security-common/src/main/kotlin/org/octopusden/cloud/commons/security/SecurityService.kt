package org.octopusden.cloud.commons.security

import org.octopusden.cloud.commons.security.config.SecurityProperties
import org.octopusden.cloud.commons.security.dto.Role
import org.octopusden.cloud.commons.security.dto.User
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(SecurityProperties::class)
open class SecurityService(private val securityProperties: SecurityProperties) {
    fun getCurrentUser(): User {
        return SecurityContextHolder.getContext()
            ?.authentication
            ?.let { authentication ->
                val username =
                    (authentication.credentials as? Jwt)?.claims?.get("preferred_username") as? String ?: ""

                val authorities = authentication.authorities ?: emptySet()
                val roles = authorities.filter { it.authority.startsWith(ROLE_PREFIX) }
                    .map { it.authority }
                    .mapNotNull { name -> securityProperties.roles[name]?.let { name to it } }
                    .map { (name, permissions) -> Role(name, permissions) }
                    .toSet()

                val groups = authorities.filter { it.authority.startsWith(GROUP_PREFIX) }
                    .map { it.authority.replace("^$GROUP_PREFIX".toRegex(), "") }
                    .toSet()
                User(username, roles, groups)
            } ?: User("anonymous", emptySet(), emptySet())
    }

    companion object {
        const val GROUP_PREFIX = "GROUP_"
        const val ROLE_PREFIX = "ROLE_"
    }
}