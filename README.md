# octopus-cloud-commons

## Goals
Common app configuration contains properties for:
role/permission model
endpoints and secrets for communications with service registry, authentication server

## Integration
```groovy
implementation(platform("org.springframework.boot:spring-boot-dependencies:${project['spring-boot.version']}"))
implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project['spring-cloud.version']}"))
implementation("com.openwaygroup.cloud-commons:security-common:1.36")
 
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.security:spring-security-oauth2-resource-server")
implementation("org.springframework.security:spring-security-oauth2-jose")
```
