package site.everyniche.streamora.data.network

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import site.everyniche.streamora.BuildConfig
import site.everyniche.streamora.data.model.ContentItem

data class WatchlistRow(
    val id: String? = null,
    val user_id: String? = null,
    val subject_id: String = "",
    val detail_path: String? = null,
    val title: String? = null,
    val cover: String? = null,
    val subject_type: Int? = null,
    val year: Int? = null,
    val rating: Double? = null,
)

data class HistoryRow(
    val id: String? = null,
    val user_id: String? = null,
    val subject_id: String = "",
    val detail_path: String? = null,
    val title: String? = null,
    val cover: String? = null,
    val subject_type: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val position_seconds: Double? = null,
    val duration_seconds: Double? = null,
    val updated_at: String? = null,
) {
    val progressPercent: Int
        get() {
            val d = duration_seconds ?: return 0
            val p = position_seconds ?: return 0
            return if (d > 0) ((p / d) * 100).toInt().coerceIn(0, 100) else 0
        }
}

private fun HistoryRow.toContentItem() = ContentItem(
    subjectId = subject_id,
    subjectType = subject_type ?: 1,
    title = title ?: "Untitled",
    cover = cover,
    detailPath = detail_path,
)

private fun WatchlistRow.toContentItem() = ContentItem(
    subjectId = subject_id,
    subjectType = subject_type ?: 1,
    title = title ?: "Untitled",
    cover = cover,
    year = year,
    rating = rating,
    detailPath = detail_path,
)

/** Direct PostgREST access to the `watchlist` / `watch_history` tables (RLS-scoped). */
class SupabaseData(private val auth: SupabaseAuth) {

    private val gson = Gson()
    private val client = NetworkModule.okHttpClient
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    private suspend fun authHeaders(): Pair<String, String>? {
        val token = auth.getAccessToken() ?: return null
        return "Authorization" to "Bearer $token"
    }

    suspend fun getWatchlist(): List<ContentItem> {
        val auth = authHeaders() ?: return emptyList()
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watchlist?select=*&order=created_at.desc")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .get()
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use emptyList()
                val text = resp.body?.string().orEmpty()
                val rows = gson.fromJson(text, Array<WatchlistRow>::class.java) ?: arrayOf()
                rows.map { it.toContentItem() }
            }
        }.getOrDefault(emptyList())
    }

    suspend fun isInWatchlist(subjectId: String): Boolean {
        val auth = authHeaders() ?: return false
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watchlist?select=id&subject_id=eq.$subjectId")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .get()
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                resp.isSuccessful && text.trim() != "[]"
            }
        }.getOrDefault(false)
    }

    suspend fun addToWatchlist(item: ContentItem): Result<Unit> = runCatching {
        val auth = authHeaders() ?: throw Exception("Not signed in")
        val userId = this.auth.getCurrentUserId() ?: throw Exception("Not signed in")
        val row = mapOf(
            "user_id" to userId,
            "subject_id" to (item.subjectId ?: item.stableId),
            "detail_path" to item.detailPath,
            "title" to item.title,
            "cover" to item.poster,
            "subject_type" to (item.subjectType ?: 1),
            "year" to item.year,
            "rating" to item.rating,
        )
        val body = gson.toJson(row).toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watchlist?on_conflict=user_id,subject_id")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Failed to add to watchlist")
        }
    }

    suspend fun removeFromWatchlist(subjectId: String): Result<Unit> = runCatching {
        val auth = authHeaders() ?: throw Exception("Not signed in")
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watchlist?subject_id=eq.$subjectId")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .delete()
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Failed to remove from watchlist")
        }
    }

    suspend fun getHistory(limit: Int = 20): List<HistoryRow> {
        val auth = authHeaders() ?: return emptyList()
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watch_history?select=*&order=updated_at.desc&limit=$limit")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .get()
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use emptyList()
                val text = resp.body?.string().orEmpty()
                (gson.fromJson(text, Array<HistoryRow>::class.java) ?: arrayOf()).toList()
            }
        }.getOrDefault(emptyList())
    }

    suspend fun recordHistory(
        item: ContentItem,
        positionSeconds: Double,
        durationSeconds: Double,
        season: Int = 0,
        episode: Int = 0,
    ) {
        val auth = authHeaders() ?: return
        val userId = this.auth.getCurrentUserId() ?: return
        val row = mapOf(
            "user_id" to userId,
            "subject_id" to (item.subjectId ?: item.stableId),
            "detail_path" to item.detailPath,
            "title" to item.title,
            "cover" to item.poster,
            "subject_type" to (item.subjectType ?: 1),
            "season" to season,
            "episode" to episode,
            "position_seconds" to positionSeconds,
            "duration_seconds" to durationSeconds,
        )
        val body = gson.toJson(row).toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$baseUrl/rest/v1/watch_history?on_conflict=user_id,subject_id,season,episode")
            .addHeader("apikey", anonKey)
            .addHeader(auth.first, auth.second)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()
        runCatching { client.newCall(req).execute().close() }
    }
}
