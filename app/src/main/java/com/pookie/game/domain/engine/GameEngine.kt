package com.pookie.game.domain.engine

interface GameEngine<TState, TMove> {
    fun createInitialState(hostId: String, partnerId: String?): TState
    fun isValidMove(state: TState, move: TMove, playerId: String): Boolean
    fun applyMove(state: TState, move: TMove, playerId: String, opponentId: String): MoveResult<TState>
}

data class MoveResult<TState>(
    val nextState: TState,
    val nextTurnPlayerId: String,
    val isGameOver: Boolean,
    val winnerId: String?,
    val isDraw: Boolean = false,
    val isExtraTurn: Boolean = false
)
