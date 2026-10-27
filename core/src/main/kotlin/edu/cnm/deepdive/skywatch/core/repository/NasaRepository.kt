package edu.cnm.deepdive.skywatch.core.repository

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import edu.cnm.deepdive.skywatch.core.api.NasaMediaApi
import edu.cnm.deepdive.skywatch.core.model.SearchResultImage
import edu.cnm.deepdive.skywatch.core.model.dto.nasa.ItemData
import edu.cnm.deepdive.skywatch.core.model.dto.nasa.Link
import edu.cnm.deepdive.skywatch.core.model.dto.nasa.SearchItem
import edu.cnm.deepdive.skywatch.core.model.dto.nasa.SearchResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URI
import java.net.URL
import java.time.OffsetDateTime
import java.util.Properties
import kotlin.jvm.java


internal object NasaRepository : MediaRepository {

    private const val SERVICE_PROPERTIES = "nasa-media-search.properties"
    private const val BASE_URL_KEY = "base_url"
    private const val LOGGING_LEVEL_KEY = "logging_level"
    private const val CANONICAL_ROLE = "canonical"
    private const val THUMBNAIL_ROLE = "preview"

    private val api: NasaMediaApi

    init {
        val properties = javaClass.classLoader
            .getResourceAsStream(SERVICE_PROPERTIES)
            ?.use { inputStream ->
                Properties().apply {
                    load(inputStream)
                }
            } ?: throw RuntimeException("$SERVICE_PROPERTIES not found on classpath")
        val baseUrl = (properties[BASE_URL_KEY]
            ?: throw RuntimeException("$BASE_URL_KEY not fount in properties.")) as String
        val loggingLevel = (properties[LOGGING_LEVEL_KEY]
            ?: throw RuntimeException("$LOGGING_LEVEL_KEY not found in properties.")) as String

        val moshi = Moshi.Builder()
            .add(OffsetDateTimeAdapter())
            .add(UrlAdapter())
            .build()

        val interceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.valueOf(loggingLevel.uppercase()))

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        api = retrofit.create(NasaMediaApi::class.java)


    }

    /**
     * Submits a search request to a media search provider, returning the results as a
     * [List][List<SearchResultImage>]&mdash;that is, as a list of image references. The order of
     * the results returned by this function is not documented by the NASA media nor are there
     * search parameters that can be used to change this order ; thus the order of the returned results is not defined.
     * requirements should be addressed by consumers (direct or indirect) of this function.
     *
     * At least one of the filter parameter values ([searchFilter],
     * [startDate], [endDate]) must be provided, or [IllegalArgumentException] will be thrown.
     * If a non-`null` value is not provided for either [startDate] or [endDate], and [searchFilter]
     * is non-`null` but [searchFilter] is blank, [IllegalArgumentException] is thrown.
     * [IllegalArgumentException] is also thrown when non-`null` values are provided for both
     * [startDate] and [endDate], but `startDate > endDate`.
     *
     *
     * One implication of the above is that a date-time range may be specified by a single endpoint.
     * For example, a `null` [startDate], combined with a non-`null` [endDate] will return images
     * for _any_ date-time less than or equal to [endDate].
     *
     * If both a date range (one- or two-sided) and a non-`null`, non-empty [searchFilter] are
     * specified, the two criteria are combined with a logical AND.
     * The NASA media API uses only the year of a specified date range in its search criteria.
     * The date -time parameters to this function must still be specified as
     * Paging is not supported by this API; the maximum size of the returned list&mdash;as well as
     * any truncation behavior&mdash;is not documented by the NASA media API, and can therefore not be assumed
     *
     * @param searchFilter optional search string.
     * @param startDate inclusive start of optional date range filter.
     * @param endDate inclusive end of optional date range filter.
     * @return [List][List<SearchResultImage>] of search results.
     *
     * @throws IllegalArgumentException If:
     *
     * - all of [searchFilter], [startDate], and [endDate] are `null`.
     * - [startDate], and [endDate] are `null`, and [searchFilter] is blank.
     * - non-`null` values are provided for both [startDate] and [endDate], but
     * `startDate > endDate` (using [OffsetDateTime.compareTo]).
     *
     * @throws Exception (with specific type specified by implementation) for any other
     * network-access or service communication failure.
     */
    override fun search(
        searchFilter: String?,
        startDate: OffsetDateTime?,
        endDate: OffsetDateTime?,
    ): List<SearchResultImage> {
        require(startDate != null || endDate != null || !searchFilter.isNullOrBlank())
        require((startDate ?: OffsetDateTime.MIN) <= (endDate ?: OffsetDateTime.MAX))
        val response: Response<SearchResult> = api.search(
            query = searchFilter,
            startYear = startDate?.year,
            endYear = endDate?.year
        )
            .execute()
        return if (!response.isSuccessful) {
            throw RuntimeException("Request was not processed successfully.")
        } else if (response.body() == null) {
            throw RuntimeException("Request did not result in a valid response.")
        } else {
            response.body()!!
                .collection
                .items
                .map(::extractResult)
        }
    }
    private fun extractResult(item: SearchItem): SearchResultImage {
        val itemData: ItemData = item.data[0]
        val links: List<Link> = item.links
        val canonical = links.firstOrNull { it.role == CANONICAL_ROLE }
            ?: links.maxBy { it.width ?: 0 }
        val thumbnail = links.firstOrNull { it.role == THUMBNAIL_ROLE }
            ?: links.minBy { it.width ?: Int.MAX_VALUE }
        return SearchResultImage(itemData.description, itemData.keywords, canonical.url, thumbnail.url)

    }
}


private class OffsetDateTimeAdapter {

    @FromJson
    fun fromJson(value: String?): OffsetDateTime? =
        value?.let { OffsetDateTime.parse(it) }

    @ToJson
    fun toJson(value: OffsetDateTime?): String? =
        value?.toString()
}

private class UrlAdapter {

    @FromJson
    fun fromJson(value: String?): URL? =
        value?.let { URI(it).toURL() }

    @ToJson
    fun toJson(value: URL?): String? =
        value?.toString()


}
