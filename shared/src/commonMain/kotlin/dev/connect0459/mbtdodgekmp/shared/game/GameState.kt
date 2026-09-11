package dev.connect0459.mbtdodgekmp.shared.game

data class GameState(
    val playerX: Int,
    val blocks: List<Block>,
    val score: Int,
    val rngSeed: Int,
    val tickCount: Int,
    val isGameOver: Boolean,
)
