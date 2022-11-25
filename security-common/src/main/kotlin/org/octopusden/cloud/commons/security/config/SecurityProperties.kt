package org.octopusden.cloud.commons.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("octopus-security")
data class SecurityProperties(val roles: Map<String, Set<String>>)