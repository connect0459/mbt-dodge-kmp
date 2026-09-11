package dev.connect0459.mbtdodgekmp.androidapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import dev.connect0459.mbtdodgekmp.shared.game.GameConfig
import dev.connect0459.mbtdodgekmp.shared.game.GameEngine
import dev.connect0459.mbtdodgekmp.shared.game.GameState
import dev.connect0459.mbtdodgekmp.shared.guest.createGameRules

private val config =
    GameConfig(
        screenWidth = 300,
        screenHeight = 500,
        playerY = 460,
        playerWidth = 50,
        blockSize = 30,
        fallSpeed = 8,
        spawnInterval = 20,
        // Must be at least fallSpeed: a block's y only ever takes multiples of
        // fallSpeed, so a smaller margin could let it step over playerY (460
        // is not itself a multiple of 8) without ever registering a collision.
        hitMargin = 8,
    )

private fun newGameState() =
    GameState(
        playerX = (config.screenWidth - config.playerWidth) / 2,
        blocks = emptyList(),
        score = 0,
        rngSeed = (System.currentTimeMillis() / 1000).toInt(),
        tickCount = 0,
        isGameOver = false,
    )

private const val MOVE_STEP = 24
private const val FRAME_INTERVAL_MS = 1000L / 30

class GameView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    var onGameStateChanged: ((GameState) -> Unit)? = null

    private val rules = createGameRules()
    private val handler = Handler(Looper.getMainLooper())
    private var state = newGameState()
    private var pendingDx = 0

    private val playerPaint = Paint().apply { color = Color.GREEN }
    private val blockPaint = Paint().apply { color = Color.RED }
    private val overlayPaint = Paint().apply { color = Color.argb(128, 0, 0, 0) }

    private val tickRunnable =
        object : Runnable {
            override fun run() {
                if (!state.isGameOver) {
                    state = GameEngine.tick(state, config, pendingDx, rules)
                    pendingDx = 0
                    onGameStateChanged?.invoke(state)
                    invalidate()
                }
                handler.postDelayed(this, FRAME_INTERVAL_MS)
            }
        }

    fun moveLeft() {
        pendingDx = -MOVE_STEP
    }

    fun moveRight() {
        pendingDx = MOVE_STEP
    }

    fun restart() {
        state = newGameState()
        onGameStateChanged?.invoke(state)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onGameStateChanged?.invoke(state)
        handler.post(tickRunnable)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaleX = width.toFloat() / config.screenWidth
        val scaleY = height.toFloat() / config.screenHeight

        canvas.drawRect(
            state.playerX * scaleX,
            config.playerY * scaleY,
            (state.playerX + config.playerWidth) * scaleX,
            (config.playerY + config.blockSize) * scaleY,
            playerPaint,
        )

        for (block in state.blocks) {
            canvas.drawRect(
                block.x * scaleX,
                block.y * scaleY,
                (block.x + config.blockSize) * scaleX,
                (block.y + config.blockSize) * scaleY,
                blockPaint,
            )
        }

        if (state.isGameOver) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        }
    }
}
