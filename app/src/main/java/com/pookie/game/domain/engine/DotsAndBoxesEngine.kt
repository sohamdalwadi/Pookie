package com.pookie.game.domain.engine

data class DotsAndBoxesState(
    val gridSize: Int = 3,
    val claimedLines: Map<String, String> = emptyMap(), // Key: lineId -> playerId
    val claimedBoxes: Map<String, String> = emptyMap(), // Key: "box_r_c" -> playerId
    val boxNumbers: Map<String, Int> = emptyMap()       // Key: "box_r_c" -> sequential box number per player (1, 2, 3...)
)

class DotsAndBoxesEngine : GameEngine<DotsAndBoxesState, String> {

    companion object {
        const val DEFAULT_GRID_SIZE = 3
    }

    override fun createInitialState(hostId: String, partnerId: String?): DotsAndBoxesState {
        return createInitialState(hostId, partnerId, DEFAULT_GRID_SIZE)
    }

    fun createInitialState(hostId: String, partnerId: String?, gridSize: Int = DEFAULT_GRID_SIZE): DotsAndBoxesState {
        val validSize = if (gridSize in 3..5) gridSize else DEFAULT_GRID_SIZE
        return DotsAndBoxesState(gridSize = validSize)
    }

    override fun isValidMove(state: DotsAndBoxesState, move: String, playerId: String): Boolean {
        return !state.claimedLines.containsKey(move) && isValidLineId(move, state.gridSize)
    }

    private fun isValidLineId(lineId: String, gridSize: Int): Boolean {
        val parts = lineId.split("_")
        if (parts.size != 3) return false
        val type = parts[0]
        val r = parts[1].toIntOrNull() ?: return false
        val c = parts[2].toIntOrNull() ?: return false

        return when (type) {
            "H" -> r in 0..gridSize && c in 0 until gridSize
            "V" -> r in 0 until gridSize && c in 0..gridSize
            else -> false
        }
    }

    override fun applyMove(
        state: DotsAndBoxesState,
        move: String,
        playerId: String,
        opponentId: String
    ): MoveResult<DotsAndBoxesState> {
        val newLines = state.claimedLines.toMutableMap()
        newLines[move] = playerId

        val newBoxes = state.claimedBoxes.toMutableMap()
        val newBoxNumbers = state.boxNumbers.toMutableMap()
        var newlyCompletedBoxes = 0
        val gridSize = state.gridSize

        // Count existing boxes captured by this player to number sequentially (1, 2, 3...)
        var playerBoxCount = newBoxes.values.count { it == playerId }

        // Check each box in the grid
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val boxKey = "box_${r}_${c}"
                if (!newBoxes.containsKey(boxKey)) {
                    val top = "H_${r}_${c}"
                    val bottom = "H_${r + 1}_${c}"
                    val left = "V_${r}_${c}"
                    val right = "V_${r}_${c + 1}"

                    if (newLines.containsKey(top) &&
                        newLines.containsKey(bottom) &&
                        newLines.containsKey(left) &&
                        newLines.containsKey(right)
                    ) {
                        newBoxes[boxKey] = playerId
                        playerBoxCount++
                        newBoxNumbers[boxKey] = playerBoxCount
                        newlyCompletedBoxes++
                    }
                }
            }
        }

        val totalBoxes = gridSize * gridSize
        val isGameOver = newBoxes.size >= totalBoxes

        val nextState = state.copy(
            gridSize = gridSize,
            claimedLines = newLines,
            claimedBoxes = newBoxes,
            boxNumbers = newBoxNumbers
        )

        if (isGameOver) {
            val player1Boxes = newBoxes.values.count { it == playerId }
            val opponentBoxes = newBoxes.values.count { it == opponentId }
            val winnerId = when {
                player1Boxes > opponentBoxes -> playerId
                opponentBoxes > player1Boxes -> opponentId
                else -> null
            }
            return MoveResult(
                nextState = nextState,
                nextTurnPlayerId = playerId,
                isGameOver = true,
                winnerId = winnerId,
                isDraw = winnerId == null
            )
        }

        // Rule: If player completed at least one box, they get another turn!
        val nextTurn = if (newlyCompletedBoxes > 0) playerId else opponentId
        return MoveResult(
            nextState = nextState,
            nextTurnPlayerId = nextTurn,
            isGameOver = false,
            winnerId = null,
            isDraw = false,
            isExtraTurn = newlyCompletedBoxes > 0
        )
    }
}
