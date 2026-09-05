package com.waw.messenger.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AuthUser(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?
)

data class AuthSession(val token: String, val expiresAt: Long)
data class AuthResult(val user: AuthUser, val session: AuthSession)

class AuthRepository(context: Context) {
    private val prefs = context.getSharedPreferences("waw_auth", Context.MODE_PRIVATE)
    private val secureStore = com.waw.messenger.security.SecureStore(context)
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    var baseUrl: String
        get() = prefs.getString("base_url", "") ?: ""
        set(value) = prefs.edit().putString("base_url", value.trim().removeSuffix("/")).apply()

    fun hasSession(): Boolean = !secureStore.get("auth_token").isNullOrBlank()
    fun token(): String? = secureStore.get("auth_token")

    suspend fun register(username: String, email: String, password: String, displayName: String): AuthResult =
        requestAuth("/auth/register", JSONObject().apply {
            put("username", username); put("email", email); put("password", password); put("displayName", displayName)
        })

    suspend fun login(identifier: String, password: String): AuthResult =
        requestAuth("/auth/login", JSONObject().apply { put("identifier", identifier); put("password", password) })

    suspend fun me(): AuthUser = withContext(Dispatchers.IO) {
        val response = request("GET", "/auth/me", null, token())
        if (!response.isSuccessful) error(readError(response))
        parseUser(JSONObject(response.body?.string().orEmpty()).getJSONObject("user"))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try { request("POST", "/auth/logout", JSONObject(), token()) }
        finally {
            secureStore.remove("auth_token")
            prefs.edit().remove("expires_at").apply()
        }
    }

    private suspend fun requestAuth(path: String, payload: JSONObject): AuthResult = withContext(Dispatchers.IO) {
        val response = request("POST", path, payload, null)
        if (!response.isSuccessful) error(readError(response))
        val root = JSONObject(response.body?.string().orEmpty())
        val user = parseUser(root.getJSONObject("user"))
        val sessionJson = root.getJSONObject("session")
        val session = AuthSession(sessionJson.getString("token"), sessionJson.getLong("expiresAt"))
        secureStore.put("auth_token", session.token)
        prefs.edit().putLong("expires_at", session.expiresAt).apply()
        AuthResult(user, session)
    }

    private fun request(method: String, path: String, payload: JSONObject?, bearer: String?): okhttp3.Response {
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) { "SERVER_URL_REQUIRED" }
        val builder = Request.Builder().url(baseUrl.trimEnd('/') + path)
        if (payload != null) builder.method(method, payload.toString().toRequestBody(jsonType)) else builder.method(method, null)
        if (!bearer.isNullOrBlank()) builder.header("Authorization", "Bearer $bearer")
        return client.newCall(builder.build()).execute()
    }

    private fun parseUser(json: JSONObject) = AuthUser(
        id = json.getString("id"), username = json.getString("username"), email = json.getString("email"),
        displayName = json.getString("displayName"), avatarUrl = json.optString("avatarUrl").ifBlank { null }
    )

    private fun readError(response: okhttp3.Response): String {
        val raw = response.body?.string().orEmpty()
        return try { JSONObject(raw).optString("error").ifBlank { "HTTP_${response.code}" } }
        catch (_: Exception) { "HTTP_${response.code}" }
    }
}
