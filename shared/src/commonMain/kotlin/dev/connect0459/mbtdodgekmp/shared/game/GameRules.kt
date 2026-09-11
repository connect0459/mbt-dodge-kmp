package dev.connect0459.mbtdodgekmp.shared.game

interface GameRules {
    fun movePlayer(
        x: Int,
        dx: Int,
        minX: Int,
        maxX: Int,
    ): Int

    fun fallStep(
        y: Int,
        speed: Int,
    ): Int

    fun isCollision(
        playerX: Int,
        playerWidth: Int,
        blockX: Int,
        blockSize: Int,
        playerY: Int,
        blockY: Int,
        hitMargin: Int,
    ): Boolean

    fun isOffScreen(
        blockY: Int,
        screenHeight: Int,
    ): Boolean

    fun shouldSpawn(
        tickCount: Int,
        interval: Int,
    ): Boolean

    fun incrementScore(score: Int): Int

    fun spawnBlock(
        seed: Int,
        minX: Int,
        maxX: Int,
    ): Pair<Int, Int>
}
