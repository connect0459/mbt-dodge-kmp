package dev.connect0459.mbtdodgekmp.shared.game

import kotlin.math.round
import kotlin.time.TimeSource

private const val SIXTY_FPS_FRAME_BUDGET_MICROSECONDS = 1_000_000.0 / 60.0

data class GameEngineBenchmarkResult(
    val label: String,
    val iterations: Int,
    val totalTickSeconds: Double,
) {
    val perTickMicroseconds: Double
        get() = totalTickSeconds / iterations * 1_000_000

    val percentOfSixtyFpsFrameBudget: Double
        get() = perTickMicroseconds / SIXTY_FPS_FRAME_BUDGET_MICROSECONDS * 100
}

fun benchmarkGameEngineTick(
    label: String,
    rules: GameRules,
    config: GameConfig,
    initialState: GameState,
    iterations: Int,
): GameEngineBenchmarkResult {
    var state = initialState
    val callStart = TimeSource.Monotonic.markNow()
    repeat(iterations) {
        state = GameEngine.tick(state, config, playerDx = 0, rules)
    }
    val totalTickSeconds = callStart.elapsedNow().inWholeNanoseconds / 1_000_000_000.0

    check(!state.isGameOver) {
        "benchmark config '$label' let the player collide; blocks must never reach the player"
    }

    return GameEngineBenchmarkResult(label, iterations, totalTickSeconds)
}

fun measureGameRulesSetupSeconds(createRules: () -> GameRules): Pair<GameRules, Double> {
    val setupStart = TimeSource.Monotonic.markNow()
    val rules = createRules()
    return rules to setupStart.elapsedNow().inWholeNanoseconds / 1_000_000_000.0
}

fun realisticGameEngineBenchmarkConfig(): GameConfig =
    GameConfig(
        screenWidth = 300,
        screenHeight = 500,
        playerY = 460,
        playerWidth = 50,
        blockSize = 30,
        fallSpeed = 8,
        spawnInterval = 20,
        hitMargin = -1,
    )

fun blockScalingGameEngineBenchmarkConfig(): GameConfig =
    GameConfig(
        screenWidth = 300,
        screenHeight = 10_000_000,
        playerY = 460,
        playerWidth = 50,
        blockSize = 30,
        fallSpeed = 8,
        spawnInterval = 1_000_000,
        hitMargin = -1,
    )

fun emptyGameEngineBenchmarkState(): GameState =
    GameState(playerX = 0, blocks = emptyList(), score = 0, rngSeed = 1, tickCount = 0, isGameOver = false)

fun gameEngineBenchmarkBlocks(count: Int): List<Block> = (0 until count).map { Block(x = it * 10, y = 0) }

fun gameEngineBenchmarkStateWithBlocks(blocks: List<Block>): GameState =
    GameState(playerX = 0, blocks = blocks, score = 0, rngSeed = 1, tickCount = 0, isGameOver = false)

private fun round3(value: Double): Double = round(value * 1000) / 1000

fun formatGameEngineBenchmarkResult(result: GameEngineBenchmarkResult): String =
    """
    ${result.label} (${result.iterations} ticks)
    total: ${round3(result.totalTickSeconds * 1000)} ms
    per tick: ${round3(result.perTickMicroseconds)} us
    % of 60fps frame budget: ${round3(result.percentOfSixtyFpsFrameBudget)}%
    """.trimIndent()

fun formatGameRulesSetupSeconds(
    label: String,
    setupSeconds: Double,
): String = "$label setup: ${round3(setupSeconds * 1000)} ms"
