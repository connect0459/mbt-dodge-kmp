package dev.connect0459.mbtdodgekmp.shared.game

object GameEngine {
    fun tick(
        state: GameState,
        config: GameConfig,
        playerDx: Int,
        rules: GameRules,
    ): GameState {
        if (state.isGameOver) return state

        val playerX = rules.movePlayer(state.playerX, playerDx, 0, config.screenWidth - config.playerWidth)
        val fallenBlocks = state.blocks.map { it.copy(y = rules.fallStep(it.y, config.fallSpeed)) }

        val collided =
            fallenBlocks.any { block ->
                rules.isCollision(
                    playerX,
                    config.playerWidth,
                    block.x,
                    config.blockSize,
                    config.playerY,
                    block.y,
                    config.hitMargin,
                )
            }

        if (collided) {
            return state.copy(playerX = playerX, blocks = fallenBlocks, isGameOver = true)
        }

        var score = state.score
        val survivingBlocks = mutableListOf<Block>()
        for (block in fallenBlocks) {
            if (rules.isOffScreen(block.y, config.screenHeight)) {
                score = rules.incrementScore(score)
            } else {
                survivingBlocks.add(block)
            }
        }

        val tickCount = state.tickCount + 1
        var rngSeed = state.rngSeed
        var blocks = survivingBlocks.toList()
        if (rules.shouldSpawn(tickCount, config.spawnInterval)) {
            val (blockX, nextSeed) = rules.spawnBlock(rngSeed, 0, config.screenWidth - config.blockSize)
            rngSeed = nextSeed
            blocks = blocks + Block(x = blockX, y = 0)
        }

        return GameState(
            playerX = playerX,
            blocks = blocks,
            score = score,
            rngSeed = rngSeed,
            tickCount = tickCount,
            isGameOver = false,
        )
    }
}
