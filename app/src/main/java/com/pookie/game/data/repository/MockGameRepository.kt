package com.pookie.game.data.repository

import com.google.gson.Gson
import com.pookie.game.domain.engine.CouplesQuizEngine
import com.pookie.game.domain.engine.CouplesQuizState
import com.pookie.game.domain.engine.DotsAndBoxesEngine
import com.pookie.game.domain.engine.DotsAndBoxesState
import com.pookie.game.domain.engine.QuizAnswerMove
import com.pookie.game.domain.engine.TicTacToeEngine
import com.pookie.game.domain.engine.TicTacToeState
import com.pookie.game.domain.model.GameStatus
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.Player
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.RematchState
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class MockGameRepository : IGameRepository {

    private val gson = Gson()
    private val ticTacToeEngine = TicTacToeEngine()
    private val dotsAndBoxesEngine = DotsAndBoxesEngine()
    private val couplesQuizEngine = CouplesQuizEngine()

    private val rooms = mutableMapOf<String, Room>()

    override suspend fun checkHealth(): Result<Boolean> = Result.success(true)

    override suspend fun createRoom(hostId: String, hostName: String, gameType: GameType): Result<Room> {
        val code = "TEST" + (1000..9999).random()
        val initialGameStateJson = when (gameType) {
            GameType.TIC_TAC_TOE -> gson.toJson(ticTacToeEngine.createInitialState(hostId, null))
            GameType.DOTS_AND_BOXES -> gson.toJson(dotsAndBoxesEngine.createInitialState(hostId, null))
            GameType.COUPLES_QUIZ -> gson.toJson(couplesQuizEngine.createInitialState(hostId, null))
        }

        val room = Room(
            roomCode = code,
            status = GameStatus.WAITING,
            gameType = gameType,
            host = Player(id = hostId, name = hostName, score = 0),
            partner = null,
            currentTurn = hostId,
            gameStateJson = initialGameStateJson,
            rematchState = RematchState(hostReady = false, partnerReady = false),
            createdAt = "Mock Time",
            lastActivity = "Mock Time",
            version = 1
        )
        rooms[code] = room
        return Result.success(room)
    }

    override suspend fun joinRoom(roomCode: String, partnerId: String, partnerName: String): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Mock room not found: $roomCode"))
        val updated = room.copy(
            status = GameStatus.IN_PROGRESS,
            partner = Player(id = partnerId, name = partnerName, score = 0),
            version = room.version + 1
        )
        rooms[roomCode] = updated
        return Result.success(updated)
    }

    override suspend fun getGameState(roomCode: String, knownVersion: Int): Result<Room?> {
        val room = rooms[roomCode]
        return Result.success(room)
    }

    override suspend fun makeMove(
        roomCode: String,
        playerId: String,
        move: Any,
        clientVersion: Int,
        piggybackReaction: ReactionType?
    ): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Room not found"))
        val partnerId = room.partner?.id ?: "partner_mock"
        val opponentId = if (playerId == room.host.id) partnerId else room.host.id

        val (nextStateJson, nextTurn, isOver, winnerId, isDraw) = when (room.gameType) {
            GameType.TIC_TAC_TOE -> {
                val state = gson.fromJson(room.gameStateJson, TicTacToeState::class.java)
                val cell = (move as? Number)?.toInt() ?: move.toString().toInt()
                val res = ticTacToeEngine.applyMove(state, cell, playerId, opponentId)
                StateDelta(gson.toJson(res.nextState), res.nextTurnPlayerId, res.isGameOver, res.winnerId, res.isDraw)
            }
            GameType.DOTS_AND_BOXES -> {
                val state = gson.fromJson(room.gameStateJson, DotsAndBoxesState::class.java)
                val lineKey = move.toString()
                val res = dotsAndBoxesEngine.applyMove(state, lineKey, playerId, opponentId)
                StateDelta(gson.toJson(res.nextState), res.nextTurnPlayerId, res.isGameOver, res.winnerId, res.isDraw)
            }
            GameType.COUPLES_QUIZ -> {
                val state = gson.fromJson(room.gameStateJson, CouplesQuizState::class.java)
                val quizMove = if (move is QuizAnswerMove) move else {
                    val map = move as? Map<*, *>
                    QuizAnswerMove(
                        questionIndex = (map?.get("questionIndex") as? Number)?.toInt() ?: 0,
                        selectedOptionIndex = (map?.get("selectedOptionIndex") as? Number)?.toInt() ?: 0
                    )
                }
                val res = couplesQuizEngine.applyMove(state, quizMove, playerId, opponentId)
                StateDelta(gson.toJson(res.nextState), res.nextTurnPlayerId, res.isGameOver, res.winnerId, res.isDraw)
            }
        }

        var newHostScore = room.host.score
        var newPartnerScore = room.partner?.score ?: 0
        if (isOver && winnerId != null) {
            if (winnerId == room.host.id) newHostScore++ else newPartnerScore++
        }

        val updated = room.copy(
            gameStateJson = nextStateJson,
            currentTurn = nextTurn,
            status = if (isOver) GameStatus.GAME_OVER else GameStatus.IN_PROGRESS,
            host = room.host.copy(score = newHostScore),
            partner = room.partner?.copy(score = newPartnerScore),
            version = room.version + 1
        )
        rooms[roomCode] = updated
        return Result.success(updated)
    }

    override suspend fun sendReaction(roomCode: String, playerId: String, reaction: ReactionType): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun requestRematch(roomCode: String, playerId: String): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Room not found"))
        val initialGameStateJson = when (room.gameType) {
            GameType.TIC_TAC_TOE -> gson.toJson(ticTacToeEngine.createInitialState(room.host.id, room.partner?.id))
            GameType.DOTS_AND_BOXES -> gson.toJson(dotsAndBoxesEngine.createInitialState(room.host.id, room.partner?.id))
            GameType.COUPLES_QUIZ -> gson.toJson(couplesQuizEngine.createInitialState(room.host.id, room.partner?.id))
        }

        val reset = room.copy(
            status = GameStatus.IN_PROGRESS,
            gameStateJson = initialGameStateJson,
            currentTurn = room.host.id,
            rematchState = RematchState(hostReady = false, partnerReady = false),
            version = room.version + 1
        )
        rooms[roomCode] = reset
        return Result.success(reset)
    }

    private data class StateDelta(
        val stateJson: String,
        val nextTurn: String,
        val isOver: Boolean,
        val winnerId: String?,
        val isDraw: Boolean
    )
}
