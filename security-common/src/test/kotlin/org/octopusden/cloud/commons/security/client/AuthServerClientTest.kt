package org.octopusden.cloud.commons.security.client

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.octopusden.cloud.commons.security.config.AuthClientProperties
import org.octopusden.cloud.commons.security.config.AuthServerProperties

/**
 * Tests for the token requests [AuthServerClient] sends to the auth server,
 * against a tiny in-process HTTP stub (no Spring context, no network).
 *
 * The load-bearing assertion: offline JWTs must be requested with BOTH the
 * "openid" and "offline_access" scopes. The client's own [AuthServerClient.getUserInfo]
 * hits the OIDC userinfo endpoint, and Keycloak serves userinfo only for
 * tokens carrying the "openid" scope — a token minted without it authenticates
 * fine but then fails authority resolution with 403 on every request.
 */
class AuthServerClientTest {
    private lateinit var server: HttpServer
    private var capturedTokenRequestBody: String? = null

    @BeforeEach
    fun startStub() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        val base = { "http://localhost:${server.address.port}" }
        server.createContext("/realms/F1/.well-known/openid-configuration") { exchange ->
            val body = """
                {
                  "token_endpoint": "${base()}/token",
                  "userinfo_endpoint": "${base()}/userinfo"
                }
            """.trimIndent().toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/token") { exchange ->
            capturedTokenRequestBody = exchange.requestBody.readBytes().decodeToString()
            val body = """
                {
                  "access_token": "stub-access",
                  "refresh_token": "stub-refresh",
                  "expires_in": 300,
                  "refresh_expires_in": 0
                }
            """.trimIndent().toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
    }

    @AfterEach
    fun stopStub() {
        server.stop(0)
    }

    private fun client(): AuthServerClient =
        AuthServerClient(
            AuthServerProperties(url = "http://localhost:${server.address.port}", realm = "F1"),
            AuthClientProperties(clientId = "test-client", clientSecret = "test-secret"),
        )

    private fun capturedParams(): Map<String, List<String>> =
        requireNotNull(capturedTokenRequestBody) { "No token request captured" }
            .split("&")
            .map { it.split("=", limit = 2) }
            .groupBy({ URLDecoder.decode(it[0], Charsets.UTF_8) }, { URLDecoder.decode(it[1], Charsets.UTF_8) })

    @Test
    @DisplayName("generateOfflineJwt requests both openid and offline_access scopes")
    fun `offline jwt scope includes openid`() {
        val jwt = client().generateOfflineJwt("alice", "secret")
        assertEquals("stub-access", jwt.accessToken)

        val scopes = capturedParams().getValue("scope").single().split(" ").toSet()
        assertTrue("offline_access" in scopes) { "offline_access scope missing: $scopes" }
        assertTrue("openid" in scopes) {
            "openid scope missing ($scopes): without it Keycloak userinfo returns 403 " +
                "and getUserInfo-based authority resolution rejects every request"
        }
    }

    @Test
    @DisplayName("generateOfflineJwt sends password grant with client credentials")
    fun `offline jwt grant shape`() {
        client().generateOfflineJwt("alice", "secret")
        val params = capturedParams()
        assertEquals("password", params.getValue("grant_type").single())
        assertEquals("alice", params.getValue("username").single())
        assertEquals("secret", params.getValue("password").single())
        assertEquals("test-client", params.getValue("client_id").single())
        assertEquals("test-secret", params.getValue("client_secret").single())
    }
}
