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
import com.pookie.game.domain.model.SoulSyncState
import com.pookie.game.domain.repository.IGameRepository

class MockGameRepository : IGameRepository {

    private val gson = Gson()
    private val ticTacToeEngine = TicTacToeEngine()
    private val dotsAndBoxesEngine = DotsAndBoxesEngine()
    private val couplesQuizEngine = CouplesQuizEngine()

    private val rooms = mutableMapOf<String, Room>()

    override suspend fun checkHealth(): Result<Boolean> = Result.success(true)

    override suspend fun createRoom(
        hostId: String,
        hostName: String,
        gameType: GameType,
        gridSize: Int,
        gender: String
    ): Result<Room> {
        val code = "TEST" + (1000..9999).random()
        val initialGameStateJson = when (gameType) {
            GameType.TIC_TAC_TOE -> gson.toJson(ticTacToeEngine.createInitialState(hostId, null))
            GameType.DOTS_AND_BOXES -> gson.toJson(dotsAndBoxesEngine.createInitialState(hostId, null, gridSize))
            GameType.COUPLES_QUIZ -> gson.toJson(couplesQuizEngine.createInitialState(hostId, null))
            GameType.SOUL_SYNC -> gson.toJson(createInitialSoulSyncState())
        }

        val room = Room(
            roomCode = code,
            status = GameStatus.WAITING,
            gameType = gameType,
            host = Player(id = hostId, name = hostName, score = 0, gender = com.pookie.game.domain.model.Gender.fromId(gender)),
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

    override suspend fun joinRoom(
        roomCode: String,
        partnerId: String,
        partnerName: String,
        gender: String
    ): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Mock room not found: $roomCode"))
        val updated = room.copy(
            status = GameStatus.IN_PROGRESS,
            partner = Player(id = partnerId, name = partnerName, score = 0, gender = com.pookie.game.domain.model.Gender.fromId(gender)),
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

        // Turn Timeout: Pass turn to opponent without mutating game state
        val isTimeoutMove = (move as? Map<*, *>)?.get("isTimeout") == true
        if (isTimeoutMove) {
            val updated = room.copy(
                currentTurn = opponentId,
                version = room.version + 1
            )
            rooms[roomCode] = updated
            return Result.success(updated)
        }

        val (nextStateJson, nextTurn, isOver, winnerId, isDraw) = when (room.gameType) {
            GameType.TIC_TAC_TOE -> {
                val state = gson.fromJson(room.gameStateJson, TicTacToeState::class.java)
                val cell = when (move) {
                    is Number -> move.toInt()
                    is Map<*, *> -> (move["cellIndex"] as? Number)?.toInt() ?: 0
                    else -> move.toString().toIntOrNull() ?: 0
                }
                val res = ticTacToeEngine.applyMove(state, cell, playerId, opponentId)
                StateDelta(gson.toJson(res.nextState), res.nextTurnPlayerId, res.isGameOver, res.winnerId, res.isDraw)
            }
            GameType.DOTS_AND_BOXES -> {
                val state = gson.fromJson(room.gameStateJson, DotsAndBoxesState::class.java)
                val lineKey = when (move) {
                    is Map<*, *> -> (move["lineKey"] as? String) ?: ""
                    else -> move.toString()
                }
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
            GameType.SOUL_SYNC -> {
                val state = gson.fromJson(room.gameStateJson, SoulSyncState::class.java) ?: SoulSyncState()
                val text = when (move) {
                    is Map<*, *> -> (move["text"] as? String) ?: ""
                    else -> move.toString()
                }
                val isHost = playerId == room.host.id
                val nextHostAnswer = if (isHost) text else state.hostAnswer
                val nextPartnerAnswer = if (!isHost) text else state.partnerAnswer
                val bothAnswered = nextHostAnswer != null && nextPartnerAnswer != null
                val nextState = state.copy(
                    hostAnswer = nextHostAnswer,
                    partnerAnswer = nextPartnerAnswer,
                    answersRevealed = bothAnswered,
                    phase = if (bothAnswered) "JUDGING" else "ANSWERING"
                )
                StateDelta(gson.toJson(nextState), opponentId ?: playerId, false, null, false)
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
            GameType.DOTS_AND_BOXES -> {
                val currentGridSize = try {
                    gson.fromJson(room.gameStateJson, DotsAndBoxesState::class.java)?.gridSize ?: 3
                } catch (_: Exception) { 3 }
                gson.toJson(dotsAndBoxesEngine.createInitialState(room.host.id, room.partner?.id, currentGridSize))
            }
            GameType.COUPLES_QUIZ -> gson.toJson(couplesQuizEngine.createInitialState(room.host.id, room.partner?.id))
            GameType.SOUL_SYNC -> gson.toJson(createInitialSoulSyncState())
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

    private fun createInitialSoulSyncState(): SoulSyncState {
        val sessionPrompts = com.pookie.game.domain.engine.SoulSyncPromptCatalog.generateSessionPrompts(10)
        val first = sessionPrompts.first()
        return SoulSyncState(
            currentRound = 1,
            totalRounds = sessionPrompts.size,
            promptText = first.template,
            targetRole = first.targetRole,
            prompts = sessionPrompts,
            phase = "ANSWERING",
            coupleScore = 0
        )
    }

    override suspend fun submitVerdict(roomCode: String, playerId: String, verdict: String): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Room not found"))
        val state = try {
            gson.fromJson(room.gameStateJson, SoulSyncState::class.java) ?: createInitialSoulSyncState()
        } catch (_: Exception) { createInitialSoulSyncState() }

        val isHost = playerId == room.host.id
        val roleKey = if (isHost) "host" else "partner"
        val newVerdicts = state.verdicts.toMutableMap().apply { put(roleKey, verdict) }
        val newReady = state.playerReady.toMutableMap().apply { put(roleKey, true) }

        var nextState = state.copy(verdicts = newVerdicts, playerReady = newReady)
        var newScore = state.coupleScore

        if (newVerdicts["host"] != null && newVerdicts["partner"] != null) {
            val isMatch = newVerdicts["host"] == "MATCH" && newVerdicts["partner"] == "MATCH"
            newScore = if (isMatch) newScore + 2 else maxOf(0, newScore - 1)
            val nextRound = state.currentRound + 1
            if (nextRound > state.totalRounds) {
                nextState = nextState.copy(
                    coupleScore = newScore,
                    completed = true
                )
            } else {
                val nextPrompt = state.prompts.getOrNull(nextRound - 1)
                nextState = SoulSyncState(
                    currentRound = nextRound,
                    totalRounds = state.totalRounds,
                    promptText = nextPrompt?.template ?: state.promptText,
                    targetRole = nextPrompt?.targetRole ?: "HOST",
                    prompts = state.prompts,
                    coupleScore = newScore,
                    lastRoundOutcome = if (isMatch) "MATCH" else "MISS"
                )
            }
        }

        val updated = room.copy(
            gameStateJson = gson.toJson(nextState),
            status = if (nextState.completed) GameStatus.GAME_OVER else GameStatus.IN_PROGRESS,
            version = room.version + 1
        )
        rooms[roomCode] = updated
        return Result.success(updated)
    }

    override suspend fun allowEva(roomCode: String): Result<Room> {
        val room = rooms[roomCode] ?: return Result.failure(Exception("Room not found"))
        val evaGender = if (room.host.gender == com.pookie.game.domain.model.Gender.MALE)
            com.pookie.game.domain.model.Gender.FEMALE else com.pookie.game.domain.model.Gender.MALE
        val updated = room.copy(
            partner = Player(id = "EVA_BOT", name = "Eva (AI)", score = 0, gender = evaGender),
            status = GameStatus.IN_PROGRESS,
            version = room.version + 1
        )
        rooms[roomCode] = updated
        return Result.success(updated)
    }

    private data class StateDelta(
        val stateJson: String,
        val nextTurn: String,
        val isOver: Boolean,
        val winnerId: String?,
        val isDraw: Boolean
    )
}
