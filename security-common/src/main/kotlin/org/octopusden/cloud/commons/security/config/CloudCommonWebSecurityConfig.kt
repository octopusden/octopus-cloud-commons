package org.octopusden.cloud.commons.security.config

import jakarta.annotation.PostConstruct
import org.octopusden.cloud.commons.security.SecurityService
import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.converter.UserInfoGrantedAuthoritiesConverter
import org.octopusden.cloud.commons.security.utils.logger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableConfigurationProperties(SecurityProperties::class)
abstract class CloudCommonWebSecurityConfig(
    private val authServerClient: AuthServerClient,
    protected val securityProperties: SecurityProperties
) {

    @PostConstruct
    fun printLoadedRoles() {
        log.info(
            "Loaded roles: {}",
            securityProperties.roles
                .map { (role, permissions) -> "$role: [${permissions.joinToString()}]" }
                .joinToString("\n")
        )
    }

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/",
                        "/actuator/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/swagger-config",
                        "/swagger-resources/**",
                        "/swagger-ui/**"
                    )
                    .permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(
                        JwtAuthenticationConverter().apply {
                            setJwtGrantedAuthoritiesConverter(
                                UserInfoGrantedAuthoritiesConverter(authServerClient)
                            )
                        }
                    )
                }
            }
            .cors { it.disable() }
        return http.build()
    }

    @Bean
    open fun securityService(): SecurityService = SecurityService(securityProperties)

    companion object {
        private val log = logger<CloudCommonWebSecurityConfig>()
    }
}
