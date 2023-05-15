package org.octopusden.cloud.commons.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import java.io.Serializable

@Suppress("MemberVisibilityCanBePrivate")
abstract class BasePermissionEvaluator(
    protected val securityService: SecurityService
) : PermissionEvaluator {

    open fun hasPermission(permission: String): Boolean {
        val (username, roles) = securityService.getCurrentUser()
        val permissions = roles.flatMap { it.permissions }
        log.trace("Check '$permission' in '$username' permissions $permissions'")
        return permissions
            .contains(permission)
            .also {
                logGrants(username, permission, "method", it)
            }
    }

    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        //could be implemented later
        return false
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable?,
        targetType: String?,
        permission: Any?
    ): Boolean {
        //could be implemented later
        return false
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(BasePermissionEvaluator::class.java)

        fun logGrants(username: String, permission: String, accessType: String, it: Boolean) {
            log.debug("User '$username' was${if (it) "" else " not"} granted permission '$permission' to $accessType access")
        }
    }
}