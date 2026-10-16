package com.pookie.game.domain.repository

import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.Room

interface IGameRepository {
    suspend fun checkHealth(): Result<Boolean>
    suspend fun createRoom(hostId: String, hostName: String, gameType: GameType, gridSize: Int = 3, gender: String = "MALE"): Result<Room>
    suspend fun joinRoom(roomCode: String, partnerId: String, partnerName: String, gender: String = "MALE"): Result<Room>
    suspend fun getGameState(roomCode: String, knownVersion: Int): Result<Room?>
    suspend fun makeMove(
        roomCode: String,
        playerId: String,
        move: Any,
        clientVersion: Int,
        piggybackReaction: ReactionType? = null
    ): Result<Room>
    suspend fun sendReaction(roomCode: String, playerId: String, reaction: ReactionType): Result<Boolean>
    suspend fun requestRematch(roomCode: String, playerId: String): Result<Room>
}
