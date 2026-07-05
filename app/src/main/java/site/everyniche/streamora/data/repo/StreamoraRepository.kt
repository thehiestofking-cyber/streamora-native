package site.everyniche.streamora.data.repo

import site.everyniche.streamora.data.model.*
import site.everyniche.streamora.data.network.NetworkModule

data class HomeData(
    val hero: List<ContentItem>,
    val trending: List<ContentItem>,
    val newReleases: List<ContentItem>,
    val topRated: List<ContentItem>,
)

/** Thin wrapper over the API that mirrors the web app's api.ts mapping logic. */
class StreamoraRepository {

    private val api = NetworkModule.api

    suspend fun getHomeData(): HomeData {
        val featured = runCatching { api.getFeatured().items.orEmpty() }.getOrDefault(emptyList())
        val trending = runCatching { api.filter(type = "movie", sort = "Hottest").items.orEmpty() }.getOrDefault(emptyList())
        val latest = runCatching { api.filter(type = "movie", sort = "Latest").items.orEmpty() }.getOrDefault(emptyList())
        val rated = runCatching { api.filter(type = "movie", sort = "Rating").items.orEmpty() }.getOrDefault(emptyList())
        val hero = (if (featured.isNotEmpty()) featured else trending).take(6)
        return HomeData(
            hero = hero,
            trending = trending,
            newReleases = latest.map { it.copy(isNew = true) },
            topRated = rated,
        )
    }

    suspend fun getCategoryData(category: String): HomeData {
        val type = when (category.lowercase()) {
            "series" -> "tv"
            "anime" -> "anime"
            else -> "movie"
        }
        if (category.equals("For You", ignoreCase = true)) return getHomeData()
        val hottest = runCatching { api.filter(type = type, sort = "Hottest").items.orEmpty() }.getOrDefault(emptyList())
        val latest = runCatching { api.filter(type = type, sort = "Latest").items.orEmpty() }.getOrDefault(emptyList())
        val rated = runCatching { api.filter(type = type, sort = "Rating").items.orEmpty() }.getOrDefault(emptyList())
        return HomeData(
            hero = (if (hottest.isNotEmpty()) hottest else rated).take(6),
            trending = hottest,
            newReleases = latest.map { it.copy(isNew = true) },
            topRated = rated,
        )
    }

    suspend fun browse(params: Map<String, String>, page: Int): List<ContentItem> = runCatching {
        api.filter(
            type = params["type"] ?: "movie",
            sort = params["sort"] ?: "Hottest",
            page = page,
            genre = params["genre"],
            country = params["country"],
        ).items.orEmpty()
    }.getOrDefault(emptyList())

    suspend fun search(query: String, type: String = "all", page: Int = 1): List<ContentItem> =
        runCatching { api.search(query, type, page).items.orEmpty() }.getOrDefault(emptyList())

    suspend fun suggest(query: String): List<String> =
        runCatching { api.suggest(query).suggestions.orEmpty().mapNotNull { it.word } }.getOrDefault(emptyList())

    suspend fun getDetails(detailPath: String): DetailsResponse? =
        runCatching { api.details(detailPath) }.getOrNull()

    suspend fun getDownloads(subjectId: String, detailPath: String, se: Int = 0, ep: Int = 0): DownloadsResponse? =
        runCatching { api.downloads(subjectId, detailPath, se, ep) }.getOrNull()
}
