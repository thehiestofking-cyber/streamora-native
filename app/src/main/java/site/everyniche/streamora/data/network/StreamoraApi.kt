package site.everyniche.streamora.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import site.everyniche.streamora.data.model.*

/** Endpoints exposed by the Streamora FastAPI backend (webapp/server.py). */
interface StreamoraApi {

    @GET("/api/home")
    suspend fun getHome(): HomeResponse

    @GET("/api/featured")
    suspend fun getFeatured(): FeaturedResponse

    @GET("/api/categories")
    suspend fun getCategories(): CategoriesResponse

    @GET("/api/filter")
    suspend fun filter(
        @Query("type") type: String,
        @Query("sort") sort: String = "Hottest",
        @Query("page") page: Int = 1,
        @Query("genre") genre: String? = null,
        @Query("country") country: String? = null,
        @Query("year") year: String? = null,
    ): FilterResponse

    @GET("/api/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "all",
        @Query("page") page: Int = 1,
    ): SearchResponse

    @GET("/api/suggest")
    suspend fun suggest(@Query("q") query: String): SuggestResponse

    @GET("/api/details")
    suspend fun details(@Query("detailPath") detailPath: String): DetailsResponse

    @GET("/api/downloads")
    suspend fun downloads(
        @Query("subjectId") subjectId: String,
        @Query("detailPath") detailPath: String,
        @Query("se") se: Int = 0,
        @Query("ep") ep: Int = 0,
    ): DownloadsResponse
}
