package dev.connect0459.mbtdodgekmp.androidapp

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dev.connect0459.mbtdodgekmp.shared.game.GameState

class MainActivity : Activity() {
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var controlsRow: LinearLayout
    private lateinit var restartButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // API 35+ enables edge-to-edge by default, which draws window content
        // behind the system status bar; without opting out here, this plain
        // (non-inset-aware) layout would render its top content underneath it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        }
        // The status bar's own icons default to a light color meant for a
        // dark backdrop; this app's light background needs dark icons instead.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        scoreText =
            TextView(this).apply {
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                setPadding(dp(16), dp(16), dp(16), dp(8))
            }

        gameView =
            GameView(this).apply {
                onGameStateChanged = { state -> updateControls(state) }
            }

        val leftButton =
            Button(this).apply {
                text = "◀"
                setOnClickListener { gameView.moveLeft() }
            }
        val rightButton =
            Button(this).apply {
                text = "▶"
                setOnClickListener { gameView.moveRight() }
            }
        controlsRow =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                addView(leftButton)
                addView(rightButton)
            }

        restartButton =
            Button(this).apply {
                text = "Restart"
                visibility = View.GONE
                setOnClickListener { gameView.restart() }
            }

        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                addView(scoreText)
                addView(gameView, LinearLayout.LayoutParams(dp(300), dp(500)))
                addView(controlsRow)
                addView(restartButton)
            }

        setContentView(root)
    }

    private fun updateControls(state: GameState) {
        scoreText.text = "Score: ${state.score}"
        controlsRow.visibility = if (state.isGameOver) View.GONE else View.VISIBLE
        restartButton.visibility = if (state.isGameOver) View.VISIBLE else View.GONE
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
