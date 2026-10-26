package edu.cnm.deepdive.skywatch.core.service

import edu.cnm.deepdive.skywatch.core.model.SearchResultImage
import edu.cnm.deepdive.skywatch.core.model.dto.nasa.SearchItem
import java.time.OffsetDateTime

interface MediaRepository {
    /**
     * Searches NASA's image and video library for media items matching the specified search
     * criteria.
     *
     * @param searchFilter Search terms used to filter results
     * @param startDate Earliest date/time for results
     * @param endDate Latest date/time for results
     * @throws IllegalArgumentException If the specified date/time range is invalid.
     */


    fun search(
        searchFilter: String? = null,
        startDate: OffsetDateTime? = null,
        endDate: OffsetDateTime? = null,
    ): List<SearchResultImage>
}