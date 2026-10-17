package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pookie.game.domain.engine.BotBehaviorMode
import com.pookie.game.domain.engine.CouplesQuizEngine
import com.pookie.game.domain.engine.CouplesQuizState
import com.pookie.game.domain.engine.DotsAndBoxesEngine
import com.pookie.game.domain.engine.DotsAndBoxesState
import com.pookie.game.domain.engine.QuizAnswerMove
import com.pookie.game.domain.engine.SoloBotEngine
import com.pookie.game.domain.engine.TicTacToeEngine
import com.pookie.game.domain.engine.TicTacToeState
import com.pookie.game.domain.model.CoupleProfile
import com.pookie.game.domain.model.GameStatus
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.model.SoulSyncState
import com.pookie.game.domain.repository.IPreferencesRepository
import com.pookie.game.domain.usecase.GetGameStateUseCase
import com.pookie.game.domain.usecase.MakeMoveUseCase
import com.pookie.game.domain.usecase.RequestRematchUseCase
import com.pookie.game.domain.usecase.SendReactionUseCase
import com.pookie.game.domain.usecase.SubmitVerdictUseCase
import com.pookie.game.presentation.common.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class GameViewEffect {
    data class ShowToast(val message: String) : GameViewEffect()
    data class PlayReactionAnimation(val isPlayer1: Boolean, val reactionType: ReactionType) : GameViewEffect()
    data class ShowRematchDialog(val isVictory: Boolean, val isDraw: Boolean, val summary: String) : GameViewEffect()
    data class ShowConnectionLostDialog(val isDisconnected: Boolean, val message: String = "Partner lost connection. Reconnecting...") : GameViewEffect()
}

class GameViewModel(
    private val getGameStateUseCase: GetGameStateUseCase,
    private val makeMoveUseCase: MakeMoveUseCase,
    private val sendReactionUseCase: SendReactionUseCase,
    private val requestRematchUseCase: RequestRematchUseCase,
    private val submitVerdictUseCase: SubmitVerdictUseCase,
    private val allowEvaUseCase: com.pookie.game.domain.usecase.AllowEvaUseCase,
    private val leaveRoomUseCase: com.pookie.game.domain.usecase.LeaveRoomUseCase,
    private val preferencesRepository: IPreferencesRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val gson = Gson()
    private val ticTacToeEngine = TicTacToeEngine()
    private val dotsAndBoxesEngine = DotsAndBoxesEngine()
    private val couplesQuizEngine = CouplesQuizEngine()

    private val _roomState = MutableStateFlow<Room?>(null)
    val roomState: StateFlow<Room?> = _roomState.asStateFlow()

    private val _ticTacToeState = MutableStateFlow(TicTacToeState())
    val ticTacToeState: StateFlow<TicTacToeState> = _ticTacToeState.asStateFlow()

    private val _dotsAndBoxesState = MutableStateFlow(DotsAndBoxesState())
    val dotsAndBoxesState: StateFlow<DotsAndBoxesState> = _dotsAndBoxesState.asStateFlow()

    private val _quizState = MutableStateFlow(CouplesQuizState())
    val quizState: StateFlow<CouplesQuizState> = _quizState.asStateFlow()

    private val _soulSyncState = MutableStateFlow(SoulSyncState())
    val soulSyncState: StateFlow<SoulSyncState> = _soulSyncState.asStateFlow()

    private val _viewEffect = MutableSharedFlow<GameViewEffect>()
    val viewEffect: SharedFlow<GameViewEffect> = _viewEffect.asSharedFlow()

    private var pollingJob: Job? = null
    val myProfile: CoupleProfile get() = preferencesRepository.getCoupleProfile()

    fun initializeRoom(initialRoom: Room) {
        _roomState.value = initialRoom
        parseGameState(initialRoom)
        if (isSoloTestMode) {
            triggerBotTurnIfNeeded()
        } else {
            startPolling(initialRoom.roomCode)
        }
    }

    private fun parseGameState(room: Room) {
        val json = room.gameStateJson ?: return
        try {
            when (room.gameType) {
                GameType.TIC_TAC_TOE -> {
                    val parsed = gson.fromJson(json, TicTacToeState::class.java)
                    if (parsed != null) _ticTacToeState.value = parsed
                }
                GameType.DOTS_AND_BOXES -> {
                    val parsed = gson.fromJson(json, DotsAndBoxesState::class.java)
                    if (parsed != null) _dotsAndBoxesState.value = parsed
                }
                GameType.COUPLES_QUIZ -> {
                    val parsed = gson.fromJson(json, CouplesQuizState::class.java)
                    if (parsed != null) _quizState.value = parsed
                }
                GameType.SOUL_SYNC -> {
                    val parsed = gson.fromJson(json, SoulSyncState::class.java)
                    if (parsed != null) _soulSyncState.value = parsed
                }
            }
        } catch (_: Exception) {}
    }

    var isSoloTestMode: Boolean = false

    fun makeMove(move: Any, piggybackReaction: ReactionType? = null) {
        val currentRoom = _roomState.value ?: return
        val isSimultaneous = currentRoom.gameType == GameType.COUPLES_QUIZ || currentRoom.gameType == GameType.SOUL_SYNC
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId

        if (!isSimultaneous && !currentRoom.isMyTurn(myId)) {
            viewModelScope.launch {
                _viewEffect.emit(GameViewEffect.ShowToast("Waiting for partner's turn!"))
            }
            return
        }

        viewModelScope.launch {
            soundManager.playMoveSound()
            val result = makeMoveUseCase(
                roomCode = currentRoom.roomCode,
                playerId = myId,
                move = move,
                clientVersion = currentRoom.version,
                piggybackReaction = piggybackReaction
            )

            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                checkMatchEnd(updated)

                if (isSoloTestMode) {
                    triggerBotTurnIfNeeded()
                }
            } else {
                _viewEffect.emit(GameViewEffect.ShowToast(result.exceptionOrNull()?.message ?: "Move failed"))
            }
        }
    }

    fun sendReaction(reaction: ReactionType) {
        val currentRoom = _roomState.value ?: return
        viewModelScope.launch {
            soundManager.playReactionSound()
            _viewEffect.emit(
                GameViewEffect.PlayReactionAnimation(
                    isPlayer1 = currentRoom.host.id == myProfile.myPlayerId,
                    reactionType = reaction
                )
            )
            sendReactionUseCase(currentRoom.roomCode, myProfile.myPlayerId, reaction)
        }
    }

    fun requestRematch() {
        val currentRoom = _roomState.value ?: return
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId
        viewModelScope.launch {
            val result = requestRematchUseCase(currentRoom.roomCode, myId)
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                _viewEffect.emit(GameViewEffect.ShowToast("Rematch requested!"))

                if (isSoloTestMode) {
                    delay(800)
                    val partnerId = currentRoom.partner?.id ?: "P_PartnerTest"
                    val botRematch = requestRematchUseCase(currentRoom.roomCode, partnerId)
                    if (botRematch.isSuccess) {
                        val rematchRoom = botRematch.getOrThrow()
                        _roomState.value = rematchRoom
                        parseGameState(rematchRoom)
                        triggerBotTurnIfNeeded()
                    }
                }
            }
        }
    }

    private fun checkMatchEnd(room: Room) {
        if (room.status == GameStatus.GAME_OVER) {
            val isHost = room.host.id == myProfile.myPlayerId
            val myScore = if (isHost) room.host.score else (room.partner?.score ?: 0)
            val oppScore = if (isHost) (room.partner?.score ?: 0) else room.host.score

            val isVictory = myScore > oppScore
            val isDraw = myScore == oppScore

            if (isVictory) soundManager.playVictorySound() else if (!isDraw) soundManager.playDefeatSound()

            val summary = when (room.gameType) {
                GameType.TIC_TAC_TOE -> if (isDraw) "Epic Drawbreaker Duel!" else if (isVictory) "Master of 3-in-a-row!" else "Well played duel!"
                GameType.DOTS_AND_BOXES -> "Territory Claim: $myScore to $oppScore boxes!"
                GameType.COUPLES_QUIZ -> _quizState.value.getHarmonyRating()
                GameType.SOUL_SYNC -> "SoulSync Harmony: ${_soulSyncState.value.coupleScore} points scored together!"
            }

            viewModelScope.launch {
                _viewEffect.emit(GameViewEffect.ShowRematchDialog(isVictory, isDraw, summary))
            }
        }
    }

    fun submitSoulSyncAnswer(text: String) {
        val currentRoom = _roomState.value ?: return
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId
        viewModelScope.launch {
            soundManager.playMoveSound()
            val result = makeMoveUseCase(
                roomCode = currentRoom.roomCode,
                playerId = myId,
                move = mapOf("text" to text),
                clientVersion = currentRoom.version
            )
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                checkMatchEnd(updated)
                if (isSoloTestMode) {
                    triggerBotTurnIfNeeded()
                }
            } else {
                _viewEffect.emit(GameViewEffect.ShowToast(result.exceptionOrNull()?.message ?: "Failed to submit answer"))
            }
        }
    }

    fun submitSoulSyncVerdict(verdict: String) {
        val currentRoom = _roomState.value ?: return
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId
        viewModelScope.launch {
            soundManager.playReactionSound()
            val result = submitVerdictUseCase(
                roomCode = currentRoom.roomCode,
                playerId = myId,
                verdict = verdict
            )
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                checkMatchEnd(updated)
                if (isSoloTestMode) {
                    triggerBotTurnIfNeeded()
                }
            } else {
                _viewEffect.emit(GameViewEffect.ShowToast(result.exceptionOrNull()?.message ?: "Failed to submit verdict"))
            }
        }
    }

    fun allowEva() {
        val currentRoom = _roomState.value ?: return
        viewModelScope.launch {
            soundManager.playReactionSound()
            val result = allowEvaUseCase(currentRoom.roomCode)
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                _viewEffect.emit(GameViewEffect.ShowToast("Eva joined the room! Game starting!"))
            } else {
                _viewEffect.emit(GameViewEffect.ShowToast(result.exceptionOrNull()?.message ?: "Failed to connect with Eva"))
            }
        }
    }

    fun leaveRoom() {
        val currentRoom = _roomState.value ?: return
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId
        pollingJob?.cancel()
        if (!isSoloTestMode) {
            viewModelScope.launch {
                leaveRoomUseCase(currentRoom.roomCode, myId)
            }
        }
    }

    fun passTurnOnTimeout() {
        val currentRoom = _roomState.value ?: return
        if (currentRoom.status != GameStatus.IN_PROGRESS) return
        val myId = if (isSoloTestMode) currentRoom.host.id else myProfile.myPlayerId
        if (!currentRoom.isMyTurn(myId)) return

        viewModelScope.launch {
            try {
                _viewEffect.emit(GameViewEffect.ShowToast("Turn timed out! Turn passed to partner."))
                val result = makeMoveUseCase(
                    roomCode = currentRoom.roomCode,
                    playerId = myId,
                    move = mapOf("isTimeout" to true),
                    clientVersion = currentRoom.version
                )
                if (result.isSuccess) {
                    val updated = result.getOrThrow()
                    _roomState.value = updated
                    parseGameState(updated)
                    checkMatchEnd(updated)
                    if (isSoloTestMode) {
                        triggerBotTurnIfNeeded()
                    }
                }
            } catch (e: Exception) {
                // Prevent any unhandled crash during timeout
            }
        }
    }

    private var botJob: Job? = null

    fun triggerBotTurnIfNeeded() {
        if (!isSoloTestMode) return
        val room = _roomState.value ?: return
        if (room.status != GameStatus.IN_PROGRESS) return
        val partnerId = room.partner?.id ?: "P_PartnerTest"
        val isPartnerTurn = room.currentTurn == partnerId || room.currentTurn.equals("PARTNER", ignoreCase = true)

        botJob?.cancel()
        botJob = viewModelScope.launch {
            when (SoloBotEngine.currentMode) {
                BotBehaviorMode.SIMULATE_LOST_CONNECTION -> {
                    delay(500)
                    _viewEffect.emit(GameViewEffect.ShowConnectionLostDialog(true, "Bot simulated: Partner lost connection."))
                    return@launch
                }
                BotBehaviorMode.SIMULATE_TIMEOUT -> {
                    // Bot intentionally does not play so human can test turn timeout
                    return@launch
                }
                BotBehaviorMode.RANDOM_CHAOS -> {
                    if (Math.random() < 0.25) {
                        _viewEffect.emit(GameViewEffect.ShowConnectionLostDialog(true, "Bot: Simulating connection lag..."))
                        delay(3500)
                        _viewEffect.emit(GameViewEffect.ShowConnectionLostDialog(false))
                    }
                }
                BotBehaviorMode.SMART -> {
                    // Continue normally
                }
            }

            when (room.gameType) {
                GameType.TIC_TAC_TOE -> {
                    if (isPartnerTurn) {
                        delay(900)
                        val tttState = _ticTacToeState.value
                        val botMoveIndex = SoloBotEngine.calculateTicTacToeMove(
                            board = tttState.board,
                            botSymbol = "O",
                            humanSymbol = "X"
                        )
                        if (botMoveIndex in 0..8) {
                            val result = makeMoveUseCase(
                                roomCode = room.roomCode,
                                playerId = partnerId,
                                move = mapOf("cellIndex" to botMoveIndex),
                                clientVersion = room.version
                            )
                            if (result.isSuccess) {
                                val updated = result.getOrThrow()
                                _roomState.value = updated
                                parseGameState(updated)
                                checkMatchEnd(updated)
                            }
                        }
                    }
                }
                GameType.DOTS_AND_BOXES -> {
                    if (isPartnerTurn) {
                        delay(900)
                        val dnbState = _dotsAndBoxesState.value
                        val lineKey = SoloBotEngine.calculateDotsAndBoxesMove(
                            claimedLines = dnbState.claimedLines,
                            claimedBoxes = dnbState.claimedBoxes,
                            gridSize = dnbState.gridSize
                        )
                        if (lineKey.isNotEmpty()) {
                            val result = makeMoveUseCase(
                                roomCode = room.roomCode,
                                playerId = partnerId,
                                move = mapOf("lineKey" to lineKey),
                                clientVersion = room.version
                            )
                            if (result.isSuccess) {
                                val updated = result.getOrThrow()
                                _roomState.value = updated
                                parseGameState(updated)
                                checkMatchEnd(updated)
                                val isStillBotTurn = updated.currentTurn == partnerId || updated.currentTurn.equals("PARTNER", ignoreCase = true)
                                if (isStillBotTurn && updated.status == GameStatus.IN_PROGRESS) {
                                    triggerBotTurnIfNeeded()
                                }
                            }
                        }
                    }
                }
                GameType.COUPLES_QUIZ -> {
                    val quiz = _quizState.value
                    if (!quiz.hasPlayerAnswered(partnerId)) {
                        delay(1200)
                        val simOption = SoloBotEngine.calculateQuizMove(quiz.currentQuestionIndex)
                        val simMove = QuizAnswerMove(quiz.currentQuestionIndex, simOption)
                        val result = makeMoveUseCase(
                            roomCode = room.roomCode,
                            playerId = partnerId,
                            move = simMove,
                            clientVersion = room.version
                        )
                        if (result.isSuccess) {
                            val updated = result.getOrThrow()
                            _roomState.value = updated
                            parseGameState(updated)
                            checkMatchEnd(updated)
                        }
                    }
                }
                GameType.SOUL_SYNC -> {
                    val ss = _soulSyncState.value
                    if (ss.phase.equals("ANSWERING", ignoreCase = true) && !ss.hasPlayerAnswered(isHost = false)) {
                        delay(1200)
                        val simAnswer = SoloBotEngine.calculateSoulSyncAnswer()
                        val result = makeMoveUseCase(
                            roomCode = room.roomCode,
                            playerId = partnerId,
                            move = mapOf("text" to simAnswer),
                            clientVersion = room.version
                        )
                        if (result.isSuccess) {
                            val updated = result.getOrThrow()
                            _roomState.value = updated
                            parseGameState(updated)
                            checkMatchEnd(updated)
                            triggerBotTurnIfNeeded()
                        }
                    } else if (ss.phase.equals("JUDGING", ignoreCase = true) && !ss.isPlayerReadyToJudge(isHost = false)) {
                        delay(1200)
                        val verdict = SoloBotEngine.calculateSoulSyncVerdict()
                        val result = submitVerdictUseCase(
                            roomCode = room.roomCode,
                            playerId = partnerId,
                            verdict = verdict
                        )
                        if (result.isSuccess) {
                            val updated = result.getOrThrow()
                            _roomState.value = updated
                            parseGameState(updated)
                            checkMatchEnd(updated)
                            triggerBotTurnIfNeeded()
                        }
                    }
                }
            }
        }
    }

    private var consecutivePollFailures = 0
    private var isDisconnectDialogVisible = false

    private fun startPolling(roomCode: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val current = _roomState.value
                val pollInterval = when {
                    current == null || current.partner == null -> 1500L
                    current.isMyTurn(myProfile.myPlayerId) -> 3000L
                    else -> 1500L
                }

                delay(pollInterval)

                val version = _roomState.value?.version ?: 0
                val result = getGameStateUseCase(roomCode, version)
                if (result.isSuccess) {
                    consecutivePollFailures = 0
                    if (isDisconnectDialogVisible) {
                        isDisconnectDialogVisible = false
                        _viewEffect.emit(GameViewEffect.ShowConnectionLostDialog(isDisconnected = false))
                    }
                    val updated = result.getOrThrow()
                    if (updated != null && (updated.version > version || (_roomState.value?.partner == null && updated.partner != null))) {
                        _roomState.value = updated
                        parseGameState(updated)
                        checkMatchEnd(updated)
                    }
                } else {
                    consecutivePollFailures++
                    // After 3 consecutive polling drops (~6-9s), alert that connection is lost
                    if (consecutivePollFailures >= 3 && !isDisconnectDialogVisible && _roomState.value?.status == GameStatus.IN_PROGRESS) {
                        isDisconnectDialogVisible = true
                        _viewEffect.emit(GameViewEffect.ShowConnectionLostDialog(isDisconnected = true))
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        soundManager.release()
    }
}
