package dev.connect0459.mbtdodgekmp.shared.game

import dev.connect0459.mbtdodgekmp.shared.guest.createGameRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameEngineBenchmarkTest {
    private val rules = createGameRules()

    @Test
    fun benchmarkGameEngineTickReportsATimingForEveryIterationInTheRealisticScenario() {
        val result =
            benchmarkGameEngineTick(
                label = "sanity check: realistic",
                rules = rules,
                config = realisticGameEngineBenchmarkConfig(),
                initialState = emptyGameEngineBenchmarkState(),
                iterations = 50,
            )

        assertEquals(50, result.iterations)
        assertTrue(result.totalTickSeconds >= 0.0)
    }

    @Test
    fun benchmarkGameEngineTickReportsATimingForEveryIterationWithPreSeededBlocks() {
        val result =
            benchmarkGameEngineTick(
                label = "sanity check: 5 blocks",
                rules = rules,
                config = blockScalingGameEngineBenchmarkConfig(),
                initialState = gameEngineBenchmarkStateWithBlocks(gameEngineBenchmarkBlocks(5)),
                iterations = 50,
            )

        assertEquals(50, result.iterations)
        assertTrue(result.totalTickSeconds >= 0.0)
    }

    @Test
    fun benchmarkGameEngineTickRejectsAConfigWhereThePlayerEndsUpColliding() {
        val collidingConfig = realisticGameEngineBenchmarkConfig().copy(hitMargin = 8)
        val stateWithABlockAtThePlayer =
            gameEngineBenchmarkStateWithBlocks(listOf(Block(x = 0, y = collidingConfig.playerY)))

        assertTrue(
            runCatching {
                benchmarkGameEngineTick(
                    label = "sanity check: must fail",
                    rules = rules,
                    config = collidingConfig,
                    initialState = stateWithABlockAtThePlayer,
                    iterations = 1,
                )
            }.isFailure,
        )
    }
}
