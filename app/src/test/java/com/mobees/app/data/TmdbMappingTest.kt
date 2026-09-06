package com.mobees.app.data

import com.mobees.app.data.model.MediaType
import com.mobees.app.data.remote.CollectionDto
import com.mobees.app.data.remote.MovieDetailDto
import com.mobees.app.data.remote.PagedResponse
import com.mobees.app.data.remote.SeasonDto
import com.mobees.app.data.remote.TmdbClient
import com.mobees.app.data.remote.TmdbTitleDto
import com.mobees.app.data.remote.TvDetailDto
import com.mobees.app.data.remote.toMovieDetail
import com.mobees.app.data.remote.toSeason
import com.mobees.app.data.remote.toSummary
import com.mobees.app.data.remote.toTvDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbMappingTest {

    private val json = TmdbClient.json

    @Test
    fun `multi search skips people and infers media type`() {
        val payload = """
            {"page":1,"results":[
              {"id":1,"media_type":"movie","title":"A Movie","release_date":"2020-05-01","vote_average":7.2,"vote_count":10,"poster_path":"/p.jpg"},
              {"id":2,"media_type":"tv","name":"A Show","first_air_date":"2011-04-17","vote_average":8.9,"vote_count":500},
              {"id":3,"media_type":"person","name":"Someone"}
            ]}
        """.trimIndent()
        val page = json.decodeFromString(PagedResponse.serializer(TmdbTitleDto.serializer()), payload)
        val summaries = page.results.mapNotNull { it.toSummary() }
        assertEquals(2, summaries.size)
        assertEquals(MediaType.MOVIE, summaries[0].mediaType)
        assertEquals("2020", summaries[0].year)
        assertEquals("https://image.tmdb.org/t/p/w500/p.jpg", summaries[0].posterUrl)
        assertEquals(MediaType.TV, summaries[1].mediaType)
        assertNull(summaries[1].posterUrl)
    }

    @Test
    fun `movie detail maps credits collection and similar`() {
        val movie = json.decodeFromString(
            MovieDetailDto.serializer(),
            """
            {"id":155,"title":"The Dark Knight","overview":"...","tagline":"Why so serious?","runtime":152,
             "release_date":"2008-07-16","vote_average":8.5,"vote_count":100,
             "genres":[{"id":28,"name":"Action"}],
             "belongs_to_collection":{"id":263,"name":"The Dark Knight Collection"},
             "credits":{"cast":[{"id":1,"name":"Christian Bale","character":"Bruce Wayne","order":0}],
                        "crew":[{"id":2,"name":"Christopher Nolan","job":"Director"},{"id":3,"name":"Someone","job":"Producer"}]},
             "similar":{"page":1,"results":[{"id":9,"title":"Sim","vote_average":7.0,"vote_count":5},{"id":10,"title":"NoVotes","vote_count":0}]}}
            """.trimIndent(),
        )
        val collection = json.decodeFromString(
            CollectionDto.serializer(),
            """{"id":263,"name":"The Dark Knight Collection","parts":[
                {"id":49026,"title":"The Dark Knight Rises","release_date":"2012-07-16","vote_average":7.8,"vote_count":10},
                {"id":272,"title":"Batman Begins","release_date":"2005-06-10","vote_average":7.7,"vote_count":10},
                {"id":155,"title":"The Dark Knight","release_date":"2008-07-16","vote_average":8.5,"vote_count":10}]}""",
        )
        val detail = movie.toMovieDetail(collection)
        assertEquals(152, detail.runtimeMinutes)
        assertEquals(listOf("Christopher Nolan"), detail.directors)
        assertEquals("Bruce Wayne", detail.cast.single().character)
        assertEquals(listOf("Batman Begins", "The Dark Knight", "The Dark Knight Rises"), detail.franchise!!.entries.map { it.title })
        assertTrue(detail.franchise!!.entries[1].isCurrent)
        assertEquals(1, detail.similar.size)
    }

    @Test
    fun `tv detail and season map into episodes`() {
        val tv = json.decodeFromString(
            TvDetailDto.serializer(),
            """{"id":1396,"name":"Breaking Bad","first_air_date":"2008-01-20","last_air_date":"2013-09-29","status":"Ended",
                "vote_average":8.9,"vote_count":1000,"episode_run_time":[47],
                "seasons":[{"season_number":0,"episode_count":3},{"season_number":1,"episode_count":7}],
                "created_by":[{"id":1,"name":"Vince Gilligan"}],"networks":[{"id":1,"name":"AMC"}]}""",
        )
        val season = json.decodeFromString(
            SeasonDto.serializer(),
            """{"id":1,"season_number":1,"name":"Season 1","episodes":[
                {"id":11,"episode_number":2,"season_number":1,"name":"Cat's in the Bag...","vote_average":8.2,"vote_count":50},
                {"id":10,"episode_number":1,"season_number":1,"name":"Pilot","vote_average":8.3,"vote_count":60,"still_path":"/s.jpg"},
                {"id":12,"episode_number":3,"season_number":1,"name":"","vote_average":0,"vote_count":0}]}""",
        ).toSeason()
        val detail = tv.toTvDetail(listOf(season))
        assertEquals(47, detail.episodeRuntimeMinutes)
        assertEquals(listOf("Vince Gilligan"), detail.creators)
        assertEquals(3, detail.episodeCount)
        assertEquals(listOf(1, 2, 3), season.episodes.map { it.episodeNumber })
        assertEquals("Episode 3", season.episodes[2].name)
        assertTrue(!season.episodes[2].isRated)
        assertEquals("https://image.tmdb.org/t/p/w300/s.jpg", season.episodes[0].stillUrl)
    }
}
