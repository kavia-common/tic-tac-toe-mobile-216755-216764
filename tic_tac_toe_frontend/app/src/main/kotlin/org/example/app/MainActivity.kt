package org.example.app

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * PUBLIC_INTERFACE
 * MainActivity is the single-screen entry point for the Tic Tac Toe game.
 * It builds a modern, rounded UI using Android Views (XML-free) to ensure compatibility without Compose.
 * It manages the game state, win/draw detection, score keeping, and provides Reset and New Game actions.
 */
class MainActivity : Activity() {

    // Ocean Professional palette
    private val colorPrimary by lazy { 0xFF2563EB.toInt() }   // Blue
    private val colorSecondary by lazy { 0xFFF59E0B.toInt() } // Amber
    private val colorError by lazy { 0xFFEF4444.toInt() }     // Red
    private val colorBackground by lazy { 0xFFF9FAFB.toInt() } // Background
    private val colorSurface by lazy { 0xFFFFFFFF.toInt() }   // Surface
    private val colorText by lazy { 0xFF111827.toInt() }      // Slate-900

    // Game state
    private val board = Array(3) { Array(3) { "" } }
    private var currentPlayer = "X"
    private var gameOver = false

    // Score tracking
    private var xWins = 0
    private var oWins = 0
    private var draws = 0

    // UI refs
    private lateinit var statusText: TextView
    private lateinit var scoreText: TextView
    private lateinit var gridLayout: GridLayout
    private val cells = Array(3) { arrayOfNulls<Button>(3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root container
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorBackground)
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }

        // Title
        val title = TextView(this).apply {
            text = "Tic Tac Toe"
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colorText)
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        })

        // Status
        statusText = TextView(this).apply {
            text = "Player X's turn"
            textSize = 16f
            setTextColor(colorPrimary)
        }
        root.addView(statusText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        })

        // Card-like surface for board
        val boardCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorSurface)
            // Elevation is supported on L+, safe here
            elevation = dp(3).toFloat()
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        // Grid layout 3x3
        gridLayout = GridLayout(this).apply {
            rowCount = 3
            columnCount = 3
            setBackgroundColor(colorSurface)
        }

        // Create 9 buttons
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val btn = Button(this).apply {
                    text = ""
                    textSize = 28f
                    setAllCaps(false)
                    setTextColor(colorText)
                    stateListAnimator = null // flatter
                    elevation = dp(2).toFloat()
                    setPadding(0, 0, 0, 0)
                    isFocusable = true
                    isSoundEffectsEnabled = true
                    setOnClickListener { onCellClicked(r, c) }
                }

                // Size each cell evenly
                val cellParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(r, 1f)
                    columnSpec = GridLayout.spec(c, 1f)
                    setMargins(dp(6), dp(6), dp(6), dp(6))
                }
                gridLayout.addView(btn, cellParams)
                cells[r][c] = btn
            }
        }

        boardCard.addView(gridLayout, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
        })

        // Scoreboard
        scoreText = TextView(this).apply {
            textSize = 16f
            setTextColor(colorText)
            text = scoreString()
            setPadding(dp(8), dp(12), dp(8), dp(12))
            gravity = Gravity.CENTER
        }
        boardCard.addView(scoreText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        // Actions container
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val resetBtn = Button(this).apply {
            text = "Reset Board"
            setTextColor(colorSurface)
            setBackgroundColor(colorPrimary)
            setOnClickListener { resetBoard(keepScores = true) }
        }

        val newGameBtn = Button(this).apply {
            text = "New Game"
            setTextColor(colorSurface)
            setBackgroundColor(colorSecondary)
            setOnClickListener { resetBoard(keepScores = false) }
        }

        actions.addView(resetBtn, LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ).apply {
            rightMargin = dp(8)
            topMargin = dp(8)
        })
        actions.addView(newGameBtn, LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ).apply {
            leftMargin = dp(8)
            topMargin = dp(8)
        })

        boardCard.addView(actions, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(4)
            bottomMargin = dp(8)
        })

        // Add the board card to root
        root.addView(boardCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
            topMargin = dp(4)
        })

        setContentView(root)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun onCellClicked(r: Int, c: Int) {
        if (gameOver) return
        if (board[r][c].isNotEmpty()) return

        board[r][c] = currentPlayer
        val btn = cells[r][c]!!
        btn.text = currentPlayer
        btn.setTextColor(if (currentPlayer == "X") colorPrimary else colorSecondary)

        // Subtle fade-in
        val anim = AlphaAnimation(0.6f, 1f).apply { duration = 120 }
        btn.startAnimation(anim)

        val winner = checkWinner()
        if (winner != null) {
            gameOver = true
            statusText.setTextColor(if (winner == "Draw") colorError else colorPrimary)
            when (winner) {
                "X" -> {
                    xWins += 1
                    statusText.text = "Player X wins!"
                }
                "O" -> {
                    oWins += 1
                    statusText.text = "Player O wins!"
                }
                else -> { // Draw
                    draws += 1
                    statusText.text = "It's a draw."
                }
            }
            updateScore()
            disableBoard()
        } else {
            togglePlayer()
            statusText.setTextColor(colorPrimary)
            statusText.text = "Player $currentPlayer's turn"
        }
    }

    private fun togglePlayer() {
        currentPlayer = if (currentPlayer == "X") "O" else "X"
    }

    private fun disableBoard() {
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                cells[r][c]?.isEnabled = false
            }
        }
    }

    private fun enableBoard() {
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                cells[r][c]?.isEnabled = true
            }
        }
    }

    private fun updateScore() {
        scoreText.text = scoreString()
    }

    private fun scoreString(): String = "Score  X: $xWins   O: $oWins   Draws: $draws"

    /**
     * PUBLIC_INTERFACE
     * Reset board and optionally scores.
     * @param keepScores When true, keeps scores but clears board; when false, also resets scores.
     */
    private fun resetBoard(keepScores: Boolean) {
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                board[r][c] = ""
                cells[r][c]?.apply {
                    text = ""
                    isEnabled = true
                }
            }
        }
        if (!keepScores) {
            xWins = 0
            oWins = 0
            draws = 0
            updateScore()
        }
        currentPlayer = "X"
        gameOver = false
        statusText.setTextColor(colorPrimary)
        statusText.text = "Player $currentPlayer's turn"
        enableBoard()
    }

    /**
     * PUBLIC_INTERFACE
     * Checks for a winner or draw.
     * @return "X" or "O" for winner; "Draw" for draw; null if game should continue.
     */
    private fun checkWinner(): String? {
        // Rows and columns
        for (i in 0 until 3) {
            if (board[i][0].isNotEmpty() && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
            if (board[0][i].isNotEmpty() && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i]
            }
        }
        // Diagonals
        if (board[0][0].isNotEmpty() && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2].isNotEmpty() && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }
        // Draw if no empty cells
        var emptyFound = false
        loop@ for (r in 0 until 3) {
            for (c in 0 until 3) {
                if (board[r][c].isEmpty()) {
                    emptyFound = true
                    break@loop
                }
            }
        }
        return if (!emptyFound) "Draw" else null
    }
}
