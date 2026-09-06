package com.mobees.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset

class StatusMathTest {

    @Test
    fun `bump starts a new day at one and increments within a day`() {
        val first = StatusMath.bump(null, "2026-09-06")
        assertEquals(DailyCount("2026-09-06", 1), first)
        assertEquals(DailyCount("2026-09-06", 2), StatusMath.bump(first, "2026-09-06"))
        assertEquals(DailyCount("2026-09-07", 1), StatusMath.bump(first, "2026-09-07"))
    }

    @Test
    fun `countFor ignores stale days`() {
        val yesterday = DailyCount("2026-09-05", 40)
        assertEquals(0, StatusMath.countFor(yesterday, "2026-09-06"))
        assertEquals(40, StatusMath.countFor(yesterday, "2026-09-05"))
        assertEquals(0, StatusMath.countFor(null, "2026-09-06"))
    }

    @Test
    fun `today formats as ISO date in the given zone`() {
        // 2026-09-06T23:30Z is still the 6th in UTC but the 7th in UTC+2.
        val millis = 1_788_737_400_000L
        assertEquals("2026-09-06", StatusMath.today(millis, ZoneOffset.UTC))
        assertEquals("2026-09-07", StatusMath.today(millis, ZoneOffset.ofHours(2)))
    }
}
