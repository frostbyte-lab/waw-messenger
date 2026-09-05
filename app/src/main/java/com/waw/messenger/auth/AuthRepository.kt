package com.waw.messenger.auth

import com.waw.messenger.BuildConfig
import com.waw.messenger.security.SecureStore

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
data class SavedAccount(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val token: String,
    val expiresAt: Long
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("username", username); put("displayName", displayName)
        put("email", email); put("token", token); put("expiresAt", expiresAt)
    }
}

class AuthRepository(context: Context) {
    private val prefs = context.getSharedPreferences("waw_auth", Context.MODE_PRIVATE)
    private val secureStore = SecureStore(context)
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    var baseUrl: String
        get() = prefs.getString("base_url", BuildConfig.WAW_API_BASE_URL) ?: BuildConfig.WAW_API_BASE_URL
        set(value) = prefs.edit().putString("base_url", value.trim().removeSuffix("/")).apply()

    fun hasSession(): Boolean = !prefs.getString("token", null).isNullOrBlank()
    fun token(): String? = prefs.getString("token", null)

    fun savedAccounts(): List<SavedAccount> = runCatching {
        val raw = secureStore.get("account_sessions") ?: return emptyList()
        val array = org.json.JSONArray(raw)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            SavedAccount(
                id = item.getString("id"),
                username = item.getString("username"),
                displayName = item.getString("displayName"),
                email = item.getString("email"),
                token = item.getString("token"),
                expiresAt = item.getLong("expiresAt")
            )
        }.filter { it.expiresAt > System.currentTimeMillis() }
    }.getOrDefault(emptyList())

    fun switchAccount(accountId: String): Boolean {
        val account = savedAccounts().firstOrNull { it.id == accountId } ?: return false
        prefs.edit().putString("token", account.token).putLong("expires_at", account.expiresAt).apply()
        return true
    }

    fun removeSavedAccount(accountId: String) {
        val retained = savedAccounts().filterNot { it.id == accountId }
        val array = org.json.JSONArray()
        retained.forEach { array.put(it.toJson()) }
        secureStore.put("account_sessions", array.toString())
    }

    suspend fun register(username: String, email: String, password: String, displayName: String): AuthResult =
        requestAuth("/auth/register", JSONObject().apply {
            put("username", username); put("email", email); put("password", password); put("displayName", displayName)
        })

    suspend fun login(identifier: String, password: String): AuthResult =
        requestAuth("/auth/login", JSONObject().apply { put("identifier", identifier); put("password", password) })

    suspend fun requestPasswordReset(identifier: String): String = withContext(Dispatchers.IO) {
        val response = request("POST", "/auth/forgot-password", JSONObject().apply { put("identifier", identifier) }, null)
        if (!response.isSuccessful) error(readError(response))
        JSONObject(response.body?.string().orEmpty()).optString("message", "Jika akun ditemukan, instruksi pemulihan akan dikirim.")
    }

    suspend fun resetPassword(resetToken: String, password: String) = withContext(Dispatchers.IO) {
        val response = request("POST", "/auth/reset-password", JSONObject().apply {
            put("token", resetToken)
            put("password", password)
        }, null)
        if (!response.isSuccessful) error(readError(response))
    }

    suspend fun me(): AuthUser = withContext(Dispatchers.IO) {
        val response = request("GET", "/auth/me", null, token())
        if (!response.isSuccessful) error(readError(response))
        parseUser(JSONObject(response.body?.string().orEmpty()).getJSONObject("user"))
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try { request("POST", "/auth/logout", JSONObject(), token()) }
        finally { prefs.edit().remove("token").remove("expires_at").apply() }
    }

    private suspend fun requestAuth(path: String, payload: JSONObject): AuthResult = withContext(Dispatchers.IO) {
        val response = request("POST", path, payload, null)
        if (!response.isSuccessful) error(readError(response))
        val root = JSONObject(response.body?.string().orEmpty())
        val user = parseUser(root.getJSONObject("user"))
        val sessionJson = root.getJSONObject("session")
        val session = AuthSession(sessionJson.getString("token"), sessionJson.getLong("expiresAt"))
        prefs.edit().putString("token", session.token).putLong("expires_at", session.expiresAt).apply()
        rememberAccount(user, session)
        AuthResult(user, session)
    }

    private fun request(method: String, path: String, payload: JSONObject?, bearer: String?): okhttp3.Response {
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) { "SERVER_URL_REQUIRED" }
        val builder = Request.Builder().url(baseUrl.trimEnd('/') + path)
        if (payload != null) builder.method(method, payload.toString().toRequestBody(jsonType)) else builder.method(method, null)
        if (!bearer.isNullOrBlank()) builder.header("Authorization", "Bearer $bearer")
        return client.newCall(builder.build()).execute()
    }

    private fun rememberAccount(user: AuthUser, session: AuthSession) {
        val accounts = savedAccounts().filterNot { it.id == user.id }.toMutableList()
        accounts.add(0, SavedAccount(user.id, user.username, user.displayName, user.email, session.token, session.expiresAt))
        val array = org.json.JSONArray()
        accounts.take(5).forEach { array.put(it.toJson()) }
        secureStore.put("account_sessions", array.toString())
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
