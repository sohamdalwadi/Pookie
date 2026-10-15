package com.pookie.game.domain.engine

data class DotsAndBoxesState(
    val claimedLines: Map<String, String> = emptyMap(), // Key: lineId -> playerId
    val claimedBoxes: Map<String, String> = emptyMap()  // Key: "box_r_c" -> playerId
)

class DotsAndBoxesEngine : GameEngine<DotsAndBoxesState, String> {

    companion object {
        const val GRID_SIZE = 3 // 3x3 boxes (4x4 dots, 9 boxes total)
    }

    override fun createInitialState(hostId: String, partnerId: String?): DotsAndBoxesState {
        return DotsAndBoxesState()
    }

    override fun isValidMove(state: DotsAndBoxesState, move: String, playerId: String): Boolean {
        return !state.claimedLines.containsKey(move) && isValidLineId(move)
    }

    private fun isValidLineId(lineId: String): Boolean {
        val parts = lineId.split("_")
        if (parts.size != 3) return false
        val type = parts[0]
        val r = parts[1].toIntOrNull() ?: return false
        val c = parts[2].toIntOrNull() ?: return false

        return when (type) {
            "H" -> r in 0..GRID_SIZE && c in 0 until GRID_SIZE
            "V" -> r in 0 until GRID_SIZE && c in 0..GRID_SIZE
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
        var newlyCompletedBoxes = 0

        // Check each of the 9 boxes
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
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
                        newlyCompletedBoxes++
                    }
                }
            }
        }

        val totalBoxes = GRID_SIZE * GRID_SIZE
        val isGameOver = newBoxes.size >= totalBoxes

        val nextState = state.copy(claimedLines = newLines, claimedBoxes = newBoxes)

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
