package dev.connect0459.mbtdodgekmp.shared.game

import dev.connect0459.mbtdodgekmp.shared.guest.createGameRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineTest {
    private val rules = createGameRules()

    private val config =
        GameConfig(
            screenWidth = 300,
            screenHeight = 600,
            playerY = 550,
            playerWidth = 40,
            blockSize = 20,
            fallSpeed = 50,
            spawnInterval = 100,
            hitMargin = 0,
        )

    @Test
    fun tickClampsThePlayerAtTheRightEdgeOfTheScreen() {
        val state =
            GameState(playerX = 250, blocks = emptyList(), score = 0, rngSeed = 1, tickCount = 0, isGameOver = false)

        val result = GameEngine.tick(state, config, playerDx = 50, rules)

        assertEquals(config.screenWidth - config.playerWidth, result.playerX)
    }

    @Test
    fun tickClampsThePlayerAtTheLeftEdgeOfTheScreen() {
        val state =
            GameState(playerX = 10, blocks = emptyList(), score = 0, rngSeed = 1, tickCount = 0, isGameOver = false)

        val result = GameEngine.tick(state, config, playerDx = -50, rules)

        assertEquals(0, result.playerX)
    }

    @Test
    fun tickEndsTheGameWhenAFallingBlockReachesThePlayer() {
        val state =
            GameState(
                playerX = 100,
                blocks = listOf(Block(x = 110, y = config.playerY - config.fallSpeed)),
                score = 0,
                rngSeed = 1,
                tickCount = 0,
                isGameOver = false,
            )

        val result = GameEngine.tick(state, config, playerDx = 0, rules)

        assertTrue(result.isGameOver)
    }

    @Test
    fun tickAwardsAPointAndRemovesABlockThatFallsPastTheScreenWithoutHittingThePlayer() {
        val state =
            GameState(
                playerX = 200,
                blocks = listOf(Block(x = 0, y = config.screenHeight - config.fallSpeed + 1)),
                score = 0,
                rngSeed = 1,
                tickCount = 0,
                isGameOver = false,
            )

        val result = GameEngine.tick(state, config, playerDx = 0, rules)

        assertEquals(1, result.score)
        assertTrue(result.blocks.isEmpty())
        assertTrue(!result.isGameOver)
    }

    @Test
    fun tickDoesNothingOnceTheGameIsAlreadyOver() {
        val state =
            GameState(playerX = 10, blocks = emptyList(), score = 5, rngSeed = 1, tickCount = 3, isGameOver = true)

        val result = GameEngine.tick(state, config, playerDx = 50, rules)

        assertEquals(state, result)
    }

    @Test
    fun tickSpawnsTheSameBlockPositionFromTheSameInitialSeed() {
        val everyTickSpawns = config.copy(spawnInterval = 1)
        val state =
            GameState(playerX = 0, blocks = emptyList(), score = 0, rngSeed = 7, tickCount = 0, isGameOver = false)

        val resultA = GameEngine.tick(state, everyTickSpawns, playerDx = 0, createGameRules())
        val resultB = GameEngine.tick(state, everyTickSpawns, playerDx = 0, createGameRules())

        assertEquals(resultA, resultB)
    }
}
