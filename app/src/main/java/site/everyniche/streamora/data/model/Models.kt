package site.everyniche.streamora.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the normalized item shape returned by the Streamora backend
 * (webapp/providers/normalizer.py -> normalize_item). Used across
 * home/featured/filter/search/category responses.
 */
data class ContentItem(
    val subjectId: String? = null,
    val subjectType: Int? = null,
    val subjectTypeName: String? = null,
    val title: String = "",
    val description: String? = null,
    val year: Int? = null,
    val genre: List<String>? = null,
    val cover: String? = null,
    val backdrop: String? = null,
    val duration: Int? = null,
    val rating: Double? = null,
    val isNew: Boolean? = null,
    val detailPath: String? = null,
    val origin: String? = null,
) {
    val isTv: Boolean get() = subjectType == 2
    val genres: List<String> get() = genre ?: emptyList()
    val poster: String? get() = cover ?: backdrop
    val ratingText: String? get() = rating?.takeIf { it > 0 }?.let { "%.1f".format(it) }
    val durationText: String?
        get() {
            val d = duration ?: return null
            if (d <= 0 || d > 100000) return null
            val h = d / 60
            val m = d % 60
            return if (h > 0) (if (m > 0) "${h}h ${m}m" else "${h}h") else "${m}m"
        }
    /** Stable identity for lists/keys. */
    val stableId: String get() = subjectId ?: detailPath ?: title
}

data class HomeResponse(
    val banners: List<ContentItem>? = null,
    val rows: List<HomeRow>? = null,
)

data class HomeRow(
    val title: String = "",
    val items: List<ContentItem>? = null,
    val genreTopId: String? = null,
)

data class FeaturedResponse(val items: List<ContentItem>? = null)

data class FilterResponse(
    val items: List<ContentItem>? = null,
    val hasMore: Boolean? = null,
    val page: Int? = null,
    val totalCount: Int? = null,
)

data class SearchResponse(
    val items: List<ContentItem>? = null,
    val hasMore: Boolean? = null,
    val page: Int? = null,
    val totalCount: Int? = null,
)

data class SuggestItem(val word: String? = null)
data class SuggestResponse(val suggestions: List<SuggestItem>? = null)

data class CategoriesResponse(val categories: List<CategoryDef>? = null)
data class CategoryDef(
    val title: String = "",
    val type: String? = null,
    val country: String? = null,
    val genre: String? = null,
    val language: String? = null,
    val origin: String? = null,
    val sort: String? = null,
)

data class CastMember(
    val name: String? = null,
    val avatar: String? = null,
    val character: String? = null,
)

data class SeasonInfo(val se: Int? = null, val maxEp: Int? = null)

data class DetailsResponse(
    val subject: ContentItem? = null,
    val stars: List<CastMember>? = null,
    val seasons: List<SeasonInfo>? = null,
)

data class DownloadOption(
    val resolution: Int? = null,
    val url: String = "",
    val size: String? = null,
)

data class Caption(
    val url: String? = null,
    val lan: String? = null,
    val lanName: String? = null,
    val language: String? = null,
)

data class DownloadsResponse(
    val downloads: List<DownloadOption>? = null,
    val captions: List<Caption>? = null,
    val streamHeaders: Map<String, String>? = null,
    val limited: Boolean? = null,
    val hasResource: Boolean? = null,
)
