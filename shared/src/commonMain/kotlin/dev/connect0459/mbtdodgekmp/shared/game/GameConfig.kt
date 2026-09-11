package dev.connect0459.mbtdodgekmp.shared.game

data class GameConfig(
    val screenWidth: Int,
    val screenHeight: Int,
    val playerY: Int,
    val playerWidth: Int,
    val blockSize: Int,
    val fallSpeed: Int,
    val spawnInterval: Int,
    val hitMargin: Int,
)
