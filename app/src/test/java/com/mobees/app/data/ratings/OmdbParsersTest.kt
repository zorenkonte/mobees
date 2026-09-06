package com.mobees.app.data.ratings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OmdbParsersTest {

    @Test
    fun `N slash A and blanks become null`() {
        assertNull(OmdbParsers.rating("N/A"))
        assertNull(OmdbParsers.rating(""))
        assertNull(OmdbParsers.rating(null))
        assertNull(OmdbParsers.votes("N/A"))
        assertNull(OmdbParsers.int("N/A"))
    }

    @Test
    fun `ratings and votes parse`() {
        assertEquals(9.5, OmdbParsers.rating("9.5")!!, 0.0001)
        assertNull(OmdbParsers.rating("0.0"))
        assertEquals(2_100_000, OmdbParsers.votes("2,100,000"))
        assertEquals(7, OmdbParsers.int("7"))
    }

    @Test
    fun `response flags`() {
        assertTrue(OmdbParsers.isSuccess("True"))
        assertFalse(OmdbParsers.isSuccess("False"))
        assertTrue(OmdbParsers.isNotFound("Series or season not found!"))
        assertFalse(OmdbParsers.isNotFound("Request limit reached!"))
    }

    @Test
    fun `season payload decodes`() {
        val dto = OmdbClient.json.decodeFromString(
            OmdbSeasonDto.serializer(),
            """{"Title":"Breaking Bad","Season":"5","totalSeasons":"5","Episodes":[
                {"Title":"Ozymandias","Released":"2013-09-15","Episode":"14","imdbRating":"10.0","imdbID":"tt2301451"},
                {"Title":"Unaired","Released":"N/A","Episode":"99","imdbRating":"N/A","imdbID":"tt0"}],
               "Response":"True"}""",
        )
        assertTrue(OmdbParsers.isSuccess(dto.response))
        assertEquals(2, dto.episodes.size)
        assertEquals(10.0, OmdbParsers.rating(dto.episodes[0].imdbRating)!!, 0.0001)
        assertNull(OmdbParsers.rating(dto.episodes[1].imdbRating))
    }

    @Test
    fun `error payload decodes`() {
        val dto = OmdbClient.json.decodeFromString(
            OmdbTitleDto.serializer(),
            """{"Response":"False","Error":"Request limit reached!"}""",
        )
        assertFalse(OmdbParsers.isSuccess(dto.response))
        assertEquals("Request limit reached!", dto.error)
    }

    @Test
    fun `default provider follows key presence`() {
        assertEquals(RatingsProvider.OMDB, defaultRatingsProvider(hasOmdbKey = true))
        assertEquals(RatingsProvider.TMDB, defaultRatingsProvider(hasOmdbKey = false))
    }
}
