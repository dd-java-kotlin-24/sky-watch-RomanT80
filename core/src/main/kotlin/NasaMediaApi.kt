import retrofit2.Call

interface NasaMediaApi {

    fun search(
        query: String = "",
        mediatype: String = "image",
        startYear: Int = 1900,
        endYear: Int = 2100
    ): Call
}