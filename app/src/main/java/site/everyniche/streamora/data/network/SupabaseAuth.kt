package site.everyniche.streamora.data.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import site.everyniche.streamora.BuildConfig

private val Context.authDataStore by preferencesDataStore("streamora_auth")

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
)

private data class AuthTokenResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user: RawUser? = null,
    val error_description: String? = null,
    val msg: String? = null,
)

private data class RawUser(
    val id: String = "",
    val email: String? = null,
    val user_metadata: Map<String, Any>? = null,
)

/**
 * Minimal Supabase Auth (GoTrue) client over plain HTTP — signup / signin /
 * signout / session persistence, without pulling in the full Supabase SDK.
 */
class SupabaseAuth(private val context: Context) {

    private val gson = Gson()
    private val client = NetworkModule.okHttpClient
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")

    val currentUserFlow: Flow<AuthUser?> = context.authDataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        AuthUser(id, prefs[KEY_USER_EMAIL], prefs[KEY_USER_NAME])
    }

    suspend fun getAccessToken(): String? = context.authDataStore.data.first()[KEY_TOKEN]

    suspend fun getCurrentUserId(): String? = context.authDataStore.data.first()[KEY_USER_ID]

    private suspend fun persistSession(resp: AuthTokenResponse) {
        val user = resp.user ?: return
        context.authDataStore.edit { prefs ->
            resp.access_token?.let { prefs[KEY_TOKEN] = it }
            prefs[KEY_USER_ID] = user.id
            user.email?.let { prefs[KEY_USER_EMAIL] = it }
            val name = (user.user_metadata?.get("display_name") as? String) ?: user.email?.substringBefore("@")
            prefs[KEY_USER_NAME] = name ?: "User"
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> = runCatching {
        val body = gson.toJson(
            mapOf("email" to email, "password" to password, "data" to mapOf("display_name" to displayName))
        ).toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$baseUrl/auth/v1/signup")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            val parsed = gson.fromJson(text, AuthTokenResponse::class.java)
            if (!resp.isSuccessful) throw Exception(parsed?.msg ?: parsed?.error_description ?: "Sign up failed")
            if (parsed?.access_token != null) persistSession(parsed)
            // If access_token is null, email confirmation is required upstream.
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        val body = gson.toJson(mapOf("email" to email, "password" to password))
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$baseUrl/auth/v1/token?grant_type=password")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            val parsed = gson.fromJson(text, AuthTokenResponse::class.java)
            if (!resp.isSuccessful || parsed?.access_token == null) {
                throw Exception(parsed?.error_description ?: parsed?.msg ?: "Invalid email or password")
            }
            persistSession(parsed)
        }
    }

    suspend fun signOut() {
        context.authDataStore.edit { it.clear() }
    }
}
