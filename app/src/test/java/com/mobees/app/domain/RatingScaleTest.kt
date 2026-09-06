package com.mobees.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingScaleTest {

    private fun red(c: Long) = (c shr 16) and 0xFF
    private fun green(c: Long) = (c shr 8) and 0xFF

    @Test
    fun `unrated uses neutral colour`() {
        assertEquals(RatingScale.UNRATED_COLOR, RatingScale.colorFor(0.0))
        assertEquals(RatingScale.UNRATED_COLOR, RatingScale.colorFor(-1.0))
    }

    @Test
    fun `low ratings are red and high ratings are green`() {
        val low = RatingScale.colorFor(3.0)
        val high = RatingScale.colorFor(9.5)
        assertTrue("low should be red-dominant", red(low) > green(low))
        assertTrue("high should be green-dominant", green(high) > red(high))
    }

    @Test
    fun `green channel increases across the mid range`() {
        val g1 = green(RatingScale.colorFor(5.5))
        val g2 = green(RatingScale.colorFor(6.5))
        val g3 = green(RatingScale.colorFor(7.5))
        assertTrue(g1 < g2)
        assertTrue(g2 <= g3 + 5) // colours plateau near yellow-green, never regress sharply
    }

    @Test
    fun `colours are opaque and clamp above ten`() {
        val c = RatingScale.colorFor(12.0)
        assertEquals(0xFFL, (c shr 24) and 0xFF)
        assertEquals(RatingScale.colorFor(10.0), c)
    }

    @Test
    fun `labels follow thresholds`() {
        assertEquals("Unrated", RatingScale.label(0.0))
        assertEquals("Poor", RatingScale.label(5.4))
        assertEquals("Average", RatingScale.label(5.5))
        assertEquals("Good", RatingScale.label(6.5))
        assertEquals("Great", RatingScale.label(7.5))
        assertEquals("Outstanding", RatingScale.label(8.5))
    }

    @Test
    fun `format uses one decimal`() {
        assertEquals("8.7", RatingScale.format(8.66))
        assertEquals("–", RatingScale.format(0.0))
    }
}
