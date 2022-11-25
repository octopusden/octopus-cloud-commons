package org.octopusden.cloud.commons.security.converter

import org.octopusden.cloud.commons.security.SecurityService
import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class UserInfoGrantedAuthoritiesConverter(
    private val authServerClient: AuthServerClient
) : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val userInfo = authServerClient.getUserInfo(jwt.tokenValue)
        val userRoles = userInfo.roles
            .map { role -> SimpleGrantedAuthority("${SecurityService.ROLE_PREFIX}$role") }
        val userGroups = userInfo.groups
            .map { group -> SimpleGrantedAuthority("${SecurityService.GROUP_PREFIX}$group") }
        return (userRoles + userGroups).toCollection(ArrayList())
    }
}