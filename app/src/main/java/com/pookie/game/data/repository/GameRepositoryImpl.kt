package com.pookie.game.data.repository

import com.google.gson.Gson
import com.pookie.game.data.api.GoogleAppsScriptApi
import com.pookie.game.data.model.dto.RoomDto
import com.pookie.game.domain.model.GameStatus
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.Player
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.RematchState
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class GameRepositoryImpl(
    private val api: GoogleAppsScriptApi
) : IGameRepository {

    private val gson = Gson()

    override suspend fun checkHealth(): Result<Boolean> = runCatching {
        val response = api.get("HEALTH_CHECK")
        if (!response.isSuccess) {
            throw Exception(response.error?.message ?: "Health check failed")
        }
        true
    }

    override suspend fun createRoom(
        hostId: String,
        hostName: String,
        gameType: GameType,
        gridSize: Int
    ): Result<Room> = runCatching {
        val payload = mapOf(
            "hostId" to hostId,
            "hostName" to hostName,
            "gameType" to gameType.id,
            "gridSize" to gridSize
        )
        val response = api.post("CREATE_ROOM", payload)
        if (!response.isSuccess || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to create room")
        }
        val dto = gson.fromJson(response.data, RoomDto::class.java)
        mapDtoToRoom(dto)
    }

    override suspend fun joinRoom(
        roomCode: String,
        partnerId: String,
        partnerName: String
    ): Result<Room> = runCatching {
        val payload = mapOf(
            "roomCode" to roomCode,
            "partnerId" to partnerId,
            "partnerName" to partnerName
        )
        val response = api.post("JOIN_ROOM", payload)
        if (!response.isSuccess || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to join room")
        }
        val dto = gson.fromJson(response.data, RoomDto::class.java)
        mapDtoToRoom(dto)
    }

    override suspend fun getGameState(roomCode: String, knownVersion: Int): Result<Room?> = runCatching {
        val params = mapOf(
            "roomCode" to roomCode,
            "version" to knownVersion.toString()
        )
        val response = api.get("GET_STATE", params)
        if (!response.isSuccess) {
            throw Exception(response.error?.message ?: "Failed to get game state")
        }
        if (response.data == null || response.data.isJsonNull) {
            return@runCatching null
        }
        val dto = gson.fromJson(response.data, RoomDto::class.java)
        mapDtoToRoom(dto)
    }

    override suspend fun makeMove(
        roomCode: String,
        playerId: String,
        move: Any,
        clientVersion: Int,
        piggybackReaction: ReactionType?
    ): Result<Room> = runCatching {
        val payload = mutableMapOf<String, Any?>(
            "roomCode" to roomCode,
            "playerId" to playerId,
            "move" to move,
            "clientVersion" to clientVersion
        )
        if (piggybackReaction != null) {
            payload["reaction"] = piggybackReaction.code
        }

        val response = api.post("MAKE_MOVE", payload)
        if (!response.isSuccess || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to make move")
        }
        val dto = gson.fromJson(response.data, RoomDto::class.java)
        mapDtoToRoom(dto)
    }

    override suspend fun sendReaction(
        roomCode: String,
        playerId: String,
        reaction: ReactionType
    ): Result<Boolean> = runCatching {
        val payload = mapOf(
            "roomCode" to roomCode,
            "playerId" to playerId,
            "emoji" to reaction.code,
            "type" to reaction.code
        )
        val response = api.post("SEND_REACTION", payload)
        if (!response.isSuccess) {
            throw Exception(response.error?.message ?: "Failed to send reaction")
        }
        true
    }

    override suspend fun requestRematch(roomCode: String, playerId: String): Result<Room> = runCatching {
        val payload = mapOf(
            "roomCode" to roomCode,
            "playerId" to playerId
        )
        val response = api.post("RESET_GAME", payload)
        if (!response.isSuccess || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to request rematch")
        }
        val dto = gson.fromJson(response.data, RoomDto::class.java)
        mapDtoToRoom(dto)
    }

    private fun mapDtoToRoom(dto: RoomDto): Room {
        return Room(
            roomCode = dto.roomCode,
            status = GameStatus.fromRaw(dto.status),
            gameType = GameType.fromId(dto.gameType),
            host = Player(
                id = dto.host.id,
                name = dto.host.name,
                score = dto.host.score ?: 0
            ),
            partner = dto.partner?.let {
                Player(
                    id = it.id,
                    name = it.name,
                    score = it.score ?: 0
                )
            },
            currentTurn = dto.currentTurn,
            gameStateJson = dto.gameState?.toString(),
            rematchState = RematchState(
                hostReady = dto.rematchState?.hostReady ?: false,
                partnerReady = dto.rematchState?.partnerReady ?: false
            ),
            createdAt = dto.createdAt,
            lastActivity = dto.lastActivity,
            version = dto.version
        )
    }
}
