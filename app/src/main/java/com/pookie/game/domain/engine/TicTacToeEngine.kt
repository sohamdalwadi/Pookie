package com.pookie.game.domain.engine

import com.google.gson.annotations.SerializedName

data class TicTacToeState(
    @SerializedName(value = "board", alternate = ["grid"])
    val board: List<String?> = List(9) { null },
    val hostSymbol: String = "X",
    val partnerSymbol: String = "O",
    val winningLine: List<Int>? = null,
    val moveHistory: List<Pair<String, Int>> = emptyList() // Pair(playerId, cellIndex)
)

class TicTacToeEngine : GameEngine<TicTacToeState, Int> {

    companion object {
        val WIN_COMBINATIONS = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                   // Diagonals
        )
    }

    override fun createInitialState(hostId: String, partnerId: String?): TicTacToeState {
        return TicTacToeState()
    }

    override fun isValidMove(state: TicTacToeState, move: Int, playerId: String): Boolean {
        if (move !in 0..8) return false
        if (state.winningLine != null) return false
        return state.board[move] == null
    }

    override fun applyMove(
        state: TicTacToeState,
        move: Int,
        playerId: String,
        opponentId: String
    ): MoveResult<TicTacToeState> {
        val isHost = state.moveHistory.count { it.first == playerId } == state.moveHistory.count { it.first == opponentId }
        val symbol = if (isHost) state.hostSymbol else state.partnerSymbol

        val newBoard = state.board.toMutableList()
        newBoard[move] = symbol

        val newHistory = state.moveHistory + Pair(playerId, move)

        // Check for winning lines
        val winLine = WIN_COMBINATIONS.find { combo ->
            val (a, b, c) = combo
            newBoard[a] != null && newBoard[a] == newBoard[b] && newBoard[b] == newBoard[c]
        }

        if (winLine != null) {
            val nextState = state.copy(board = newBoard, winningLine = winLine, moveHistory = newHistory)
            return MoveResult(
                nextState = nextState,
                nextTurnPlayerId = playerId,
                isGameOver = true,
                winnerId = playerId,
                isDraw = false
            )
        }

        // Check for Draw (board full with no winner)
        val isFull = newBoard.all { it != null }
        if (isFull) {
            val nextState = state.copy(board = newBoard, winningLine = null, moveHistory = newHistory)
            return MoveResult(
                nextState = nextState,
                nextTurnPlayerId = opponentId,
                isGameOver = true,
                winnerId = null,
                isDraw = true
            )
        }

        // Regular move - switch turns
        val nextState = state.copy(board = newBoard, moveHistory = newHistory)
        return MoveResult(
            nextState = nextState,
            nextTurnPlayerId = opponentId,
            isGameOver = false,
            winnerId = null,
            isDraw = false
        )
    }
}
