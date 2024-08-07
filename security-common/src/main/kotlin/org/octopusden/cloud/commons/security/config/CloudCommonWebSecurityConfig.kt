package org.octopusden.cloud.commons.security.config

import javax.annotation.PostConstruct
import org.octopusden.cloud.commons.security.SecurityService
import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.converter.UserInfoGrantedAuthoritiesConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableConfigurationProperties(SecurityProperties::class)
abstract class CloudCommonWebSecurityConfig(private val authServerClient: AuthServerClient) {
    @Autowired
    protected lateinit var securityProperties: SecurityProperties

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
            .authorizeRequests()
            .antMatchers(
                "/",
                "/actuator/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/v3/api-docs/swagger-config",
                "/swagger-resources/**",
                "/swagger-ui/**"
            ).permitAll()
            .anyRequest().authenticated()
            .and().oauth2ResourceServer {
                it.jwt()
                    .jwtAuthenticationConverter(
                        with(JwtAuthenticationConverter()) {
                            setJwtGrantedAuthoritiesConverter(
                                UserInfoGrantedAuthoritiesConverter(
                                    authServerClient
                                )
                            )
                            this
                        })
            }
            .cors().disable()
        return http.build()
    }

    @Bean
    open fun securityService(): SecurityService = SecurityService(securityProperties)

    companion object {
        private val log = LoggerFactory.getLogger(CloudCommonWebSecurityConfig::class.java)
    }
}
