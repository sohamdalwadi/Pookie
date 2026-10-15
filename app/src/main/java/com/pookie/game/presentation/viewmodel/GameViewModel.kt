package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pookie.game.domain.engine.CouplesQuizEngine
import com.pookie.game.domain.engine.CouplesQuizState
import com.pookie.game.domain.engine.DotsAndBoxesEngine
import com.pookie.game.domain.engine.DotsAndBoxesState
import com.pookie.game.domain.engine.QuizAnswerMove
import com.pookie.game.domain.engine.TicTacToeEngine
import com.pookie.game.domain.engine.TicTacToeState
import com.pookie.game.domain.model.CoupleProfile
import com.pookie.game.domain.model.GameStatus
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IPreferencesRepository
import com.pookie.game.domain.usecase.GetGameStateUseCase
import com.pookie.game.domain.usecase.MakeMoveUseCase
import com.pookie.game.domain.usecase.RequestRematchUseCase
import com.pookie.game.domain.usecase.SendReactionUseCase
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
}

class GameViewModel(
    private val getGameStateUseCase: GetGameStateUseCase,
    private val makeMoveUseCase: MakeMoveUseCase,
    private val sendReactionUseCase: SendReactionUseCase,
    private val requestRematchUseCase: RequestRematchUseCase,
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

    private val _viewEffect = MutableSharedFlow<GameViewEffect>()
    val viewEffect: SharedFlow<GameViewEffect> = _viewEffect.asSharedFlow()

    private var pollingJob: Job? = null
    val myProfile: CoupleProfile get() = preferencesRepository.getCoupleProfile()

    fun initializeRoom(initialRoom: Room) {
        _roomState.value = initialRoom
        parseGameState(initialRoom)
        startPolling(initialRoom.roomCode)
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
            }
        } catch (_: Exception) {}
    }

    fun makeMove(move: Any, piggybackReaction: ReactionType? = null) {
        val currentRoom = _roomState.value ?: return
        if (currentRoom.currentTurn != myProfile.myPlayerId) {
            viewModelScope.launch {
                _viewEffect.emit(GameViewEffect.ShowToast("Waiting for partner's turn!"))
            }
            return
        }

        viewModelScope.launch {
            soundManager.playMoveSound()
            val result = makeMoveUseCase(
                roomCode = currentRoom.roomCode,
                playerId = myProfile.myPlayerId,
                move = move,
                clientVersion = currentRoom.version,
                piggybackReaction = piggybackReaction
            )

            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                checkMatchEnd(updated)
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
        viewModelScope.launch {
            val result = requestRematchUseCase(currentRoom.roomCode, myProfile.myPlayerId)
            if (result.isSuccess) {
                val updated = result.getOrThrow()
                _roomState.value = updated
                parseGameState(updated)
                _viewEffect.emit(GameViewEffect.ShowToast("Rematch requested!"))
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
            }

            viewModelScope.launch {
                _viewEffect.emit(GameViewEffect.ShowRematchDialog(isVictory, isDraw, summary))
            }
        }
    }

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
                    val updated = result.getOrThrow()
                    if (updated != null && updated.version > version) {
                        _roomState.value = updated
                        parseGameState(updated)
                        checkMatchEnd(updated)
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
