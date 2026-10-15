package com.pookie.game.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicTacToeEngineTest {

    private val engine = TicTacToeEngine()

    @Test
    fun `test initial state is clean`() {
        val state = engine.createInitialState("host123", "partner456")
        assertEquals(9, state.board.size)
        assertTrue(state.board.all { it == null })
        assertNull(state.winningLine)
    }

    @Test
    fun `test valid horizontal win for host`() {
        var state = engine.createInitialState("host123", "partner456")

        // Move 0: Host plays 0 (row 1, col 1)
        var result = engine.applyMove(state, 0, "host123", "partner456")
        state = result.nextState
        assertFalse(result.isGameOver)

        // Move 1: Partner plays 3 (row 2, col 1)
        result = engine.applyMove(state, 3, "partner456", "host123")
        state = result.nextState
        assertFalse(result.isGameOver)

        // Move 2: Host plays 1 (row 1, col 2)
        result = engine.applyMove(state, 1, "host123", "partner456")
        state = result.nextState
        assertFalse(result.isGameOver)

        // Move 3: Partner plays 4 (row 2, col 2)
        result = engine.applyMove(state, 4, "partner456", "host123")
        state = result.nextState
        assertFalse(result.isGameOver)

        // Move 4: Host plays 2 (row 1, col 3) -> Win!
        result = engine.applyMove(state, 2, "host123", "partner456")
        assertTrue(result.isGameOver)
        assertEquals("host123", result.winnerId)
        assertFalse(result.isDraw)
        assertNotNull(result.nextState.winningLine)
        assertEquals(listOf(0, 1, 2), result.nextState.winningLine)
    }

    @Test
    fun `test draw condition when all cells filled`() {
        // Simulating full draw:
        // X O X
        // X O O
        // O X X
        // 0:X, 1:O, 2:X, 3:X, 4:O, 5:O, 6:O, 7:X, 8:X
        var state = engine.createInitialState("host", "partner")

        val moves = listOf(
            Pair("host", 0), Pair("partner", 1), Pair("host", 2),
            Pair("partner", 4), Pair("host", 3), Pair("partner", 5),
            Pair("host", 7), Pair("partner", 6), Pair("host", 8)
        )

        var result: MoveResult<TicTacToeState>? = null
        for ((player, cell) in moves) {
            val opp = if (player == "host") "partner" else "host"
            result = engine.applyMove(state, cell, player, opp)
            state = result.nextState
        }

        assertNotNull(result)
        assertTrue(result!!.isGameOver)
        assertTrue(result.isDraw)
        assertNull(result.winnerId)
    }
}
