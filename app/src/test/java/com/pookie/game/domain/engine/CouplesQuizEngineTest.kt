package com.pookie.game.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CouplesQuizEngineTest {

    private val engine = CouplesQuizEngine()

    @Test
    fun `test matching answers increment harmony score and advances question`() {
        var state = engine.createInitialState("partnerA", "partnerB")

        // Partner A answers Q0 option 1
        var result = engine.applyMove(
            state,
            QuizAnswerMove(questionIndex = 0, selectedOptionIndex = 1),
            "partnerA",
            "partnerB"
        )
        state = result.nextState
        assertFalse(state.isCurrentQuestionRevealed)
        assertEquals(0, state.currentQuestionIndex)

        // Partner B answers same option 1
        result = engine.applyMove(
            state,
            QuizAnswerMove(questionIndex = 0, selectedOptionIndex = 1),
            "partnerB",
            "partnerA"
        )
        state = result.nextState

        assertTrue(state.isQuestionRevealed(0))
        assertEquals(1, state.matchScore) // Match!
        assertEquals(1, state.currentQuestionIndex) // Advanced to Q1
        assertFalse(result.isGameOver)
    }

    @Test
    fun `test differing answers do not increment score`() {
        var state = engine.createInitialState("partnerA", "partnerB")

        // Partner A picks 0, Partner B picks 2
        state = engine.applyMove(state, QuizAnswerMove(0, 0), "partnerA", "partnerB").nextState
        val result = engine.applyMove(state, QuizAnswerMove(0, 2), "partnerB", "partnerA")

        assertEquals(0, result.nextState.matchScore)
        assertEquals(1, result.nextState.currentQuestionIndex)
    }
}
