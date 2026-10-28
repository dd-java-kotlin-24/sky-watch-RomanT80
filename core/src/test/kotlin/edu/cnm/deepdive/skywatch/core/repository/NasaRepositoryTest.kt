package edu.cnm.deepdive.skywatch.core.repository

import edu.cnm.deepdive.skywatch.core.model.SearchResultImage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.time.OffsetDateTime

class NasaRepositoryTest {
    @Test
    fun search() {
        val searchResults: List<SearchResultImage> = NasaRepository.search(
            searchFilter = "Sputnik",
            startDate = OffsetDateTime.parse("2015-01-01T00:00:00Z")
        )
        println(searchResults)
    }

    @ParameterizedTest
    @CsvFileSource(
        resources = ["search-invalid.csv"],
        useHeadersInDisplayName = true,
        nullValues = ["null"]
    )
    fun `search throws exception on invalid arguments`(
        searchFilter: String?,
        startDate: OffsetDateTime?,
        endDate: OffsetDateTime?,
        expectedException: Class<out Throwable>
    ) {

    }

}