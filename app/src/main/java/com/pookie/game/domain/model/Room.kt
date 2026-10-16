package com.pookie.game.domain.model

data class Room(
    val roomCode: String,
    val status: GameStatus,
    val gameType: GameType,
    val host: Player,
    val partner: Player?,
    val currentTurn: String,
    val gameStateJson: String?,
    val rematchState: RematchState,
    val createdAt: String?,
    val lastActivity: String?,
    val version: Int
) {
    val isFull: Boolean get() = partner != null
    fun isMyTurn(myPlayerId: String): Boolean {
        if (currentTurn == myPlayerId) return true
        if (currentTurn.equals("HOST", ignoreCase = true) && host.id == myPlayerId) return true
        if (currentTurn.equals("PARTNER", ignoreCase = true) && partner?.id == myPlayerId) return true
        return false
    }
    fun getOpponent(myPlayerId: String): Player? = if (host.id == myPlayerId) partner else host
    fun getPlayer(playerId: String): Player? = if (host.id == playerId) host else if (partner?.id == playerId) partner else null
}
