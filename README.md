# octopus-cloud-commons

## Goals
Cloud Commons library unifies security flow for microservices based on Spring Cloud/Spring Boot frameworks

## Usage
### build.gradle
```groovy
implementation(platform("org.springframework.boot:spring-boot-dependencies:${project['spring-boot.version']}"))
implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project['spring-cloud.version']}"))
implementation("com.openwaygroup.cloud-commons:security-common:1.36")

implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.security:spring-security-oauth2-resource-server")
implementation("org.springframework.security:spring-security-oauth2-jose")
```

### WebSecurityConfig.kt
```kotlin
@Configuration
@Import(AuthServerClient::class)
class WebSecurityConfig(authServerClient: AuthServerClient) : F1CloudCommonWebSecurityConfig(authServerClient)
```

### application.yml
```yaml
security:
  roles:
    ROLE_ADMIN:
      - ACCESS_CONFIGURATION
      - ACCESS_META
      - ACCESS_DOC
      - PUBLISH_DOC
      - DELETE_DOC
    ROLE_USER:
      - ACCESS_META
      - ACCESS_DOC
    ROLE_PUBLISHER:
      - PUBLISH_DOC

```
