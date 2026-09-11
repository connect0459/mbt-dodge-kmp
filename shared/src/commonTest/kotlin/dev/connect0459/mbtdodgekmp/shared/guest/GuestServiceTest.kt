package dev.connect0459.mbtdodgekmp.shared.guest

import kotlin.test.Test
import kotlin.test.assertEquals

class GuestServiceTest {
    private val guestService = createGuestService()

    @Test
    fun movePlayerMovesWithinBoundsByTheGivenDelta() {
        assertEquals(55, guestService.movePlayer(50, 5, 0, 100))
    }

    @Test
    fun movePlayerClampsAtTheUpperBound() {
        assertEquals(100, guestService.movePlayer(98, 10, 0, 100))
    }

    @Test
    fun movePlayerClampsAtTheLowerBound() {
        assertEquals(0, guestService.movePlayer(2, -10, 0, 100))
    }

    @Test
    fun fallStepAdvancesYByTheGivenSpeed() {
        assertEquals(13, guestService.fallStep(10, 3))
    }

    @Test
    fun isCollisionDetectsOverlappingPlayerAndBlock() {
        assertEquals(1, guestService.isCollision(10, 20, 15, 10, 100, 100, 0))
    }

    @Test
    fun isCollisionReportsNoOverlapWhenHorizontallySeparated() {
        assertEquals(0, guestService.isCollision(10, 20, 100, 10, 100, 100, 0))
    }

    @Test
    fun isCollisionReportsNoOverlapWhenVerticallySeparatedBeyondMargin() {
        assertEquals(0, guestService.isCollision(10, 20, 15, 10, 100, 50, 5))
    }

    @Test
    fun isCollisionRespectsTheHitMarginWhenCheckingVerticalDistance() {
        assertEquals(1, guestService.isCollision(10, 20, 15, 10, 100, 95, 5))
    }

    @Test
    fun isCollisionDetectsOverlapWhenTheBlockIsBelowThePlayerWithinMargin() {
        assertEquals(1, guestService.isCollision(10, 20, 15, 10, 95, 100, 5))
    }

    @Test
    fun isOffScreenReportsTrueOnceABlockPassesTheScreenHeight() {
        assertEquals(1, guestService.isOffScreen(650, 600))
    }

    @Test
    fun isOffScreenReportsFalseWhileABlockIsStillWithinTheScreen() {
        assertEquals(0, guestService.isOffScreen(600, 600))
    }

    @Test
    fun shouldSpawnFiresOnEveryTickThatIsAMultipleOfTheInterval() {
        assertEquals(1, guestService.shouldSpawn(30, 30))
    }

    @Test
    fun shouldSpawnStaysSilentBetweenIntervals() {
        assertEquals(0, guestService.shouldSpawn(31, 30))
    }

    @Test
    fun shouldSpawnFiresOnTickZero() {
        assertEquals(1, guestService.shouldSpawn(0, 30))
    }

    @Test
    fun incrementScoreAddsOneToTheCurrentScore() {
        assertEquals(10, guestService.incrementScore(9))
    }
}
