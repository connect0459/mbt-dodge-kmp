package dev.connect0459.mbtdodgekmp.shared.game

import dev.connect0459.mbtdodgekmp.shared.guest.createGameRules
import kotlin.test.Test

class GameEngineBenchmarkJvmTest {
    @Test
    fun printGameEngineTickBenchmarksAtRealScale() {
        val (rules, setupSeconds) = measureGameRulesSetupSeconds { createGameRules() }
        println(formatGameRulesSetupSeconds("createGameRules()", setupSeconds))

        println(
            formatGameEngineBenchmarkResult(
                benchmarkGameEngineTick(
                    label = "steady-state play (androidApp config, spawnInterval=20)",
                    rules = rules,
                    config = realisticGameEngineBenchmarkConfig(),
                    initialState = emptyGameEngineBenchmarkState(),
                    iterations = 20_000,
                ),
            ),
        )

        for (blockCount in listOf(1, 5, 20)) {
            println(
                formatGameEngineBenchmarkResult(
                    benchmarkGameEngineTick(
                        label = "$blockCount simultaneous block(s), no spawn/off-screen churn",
                        rules = rules,
                        config = blockScalingGameEngineBenchmarkConfig(),
                        initialState = gameEngineBenchmarkStateWithBlocks(gameEngineBenchmarkBlocks(blockCount)),
                        iterations = 5_000,
                    ),
                ),
            )
        }
    }
}
