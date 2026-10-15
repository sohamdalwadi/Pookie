package com.pookie.game.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DotsAndBoxesEngineTest {

    private val engine = DotsAndBoxesEngine()

    @Test
    fun `test completing box awards box to player and grants extra turn`() {
        var state = engine.createInitialState("p1", "p2")

        // 3 borders of top-left box (box_0_0) are placed
        state = engine.applyMove(state, "H_0_0", "p1", "p2").nextState // top
        state = engine.applyMove(state, "H_1_0", "p2", "p1").nextState // bottom
        state = engine.applyMove(state, "V_0_0", "p1", "p2").nextState // left

        // Player 2 places 4th line (right border: V_0_1)
        val result = engine.applyMove(state, "V_0_1", "p2", "p1")

        assertTrue(result.isExtraTurn)
        assertEquals("p2", result.nextTurnPlayerId) // Extra turn granted!
        assertEquals("p2", result.nextState.claimedBoxes["box_0_0"])
        assertEquals(1, result.nextState.claimedBoxes.size)
        assertFalse(result.isGameOver)
    }

    @Test
    fun `test invalid duplicate line rejected`() {
        val state = engine.createInitialState("p1", "p2")
        val nextState = engine.applyMove(state, "H_0_0", "p1", "p2").nextState
        assertFalse(engine.isValidMove(nextState, "H_0_0", "p2"))
    }
}
