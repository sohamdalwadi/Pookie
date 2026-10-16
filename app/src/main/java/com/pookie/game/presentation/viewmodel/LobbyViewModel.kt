package com.pookie.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pookie.game.domain.model.CoupleProfile
import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.OtaUpdateInfo
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IPreferencesRepository
import com.pookie.game.domain.usecase.CheckOtaUpdateUseCase
import com.pookie.game.domain.usecase.CreateRoomUseCase
import com.pookie.game.domain.usecase.JoinRoomUseCase
import com.pookie.game.presentation.common.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LobbyViewModel(
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val checkOtaUpdateUseCase: CheckOtaUpdateUseCase,
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    private val _roomActionState = MutableStateFlow<Resource<Room>>(Resource.Idle)
    val roomActionState: StateFlow<Resource<Room>> = _roomActionState.asStateFlow()

    private val _otaState = MutableStateFlow<OtaUpdateInfo?>(null)
    val otaState: StateFlow<OtaUpdateInfo?> = _otaState.asStateFlow()

    val profile: CoupleProfile get() = preferencesRepository.getCoupleProfile()

    fun createRoom(gameType: GameType, gridSize: Int = 3) {
        viewModelScope.launch {
            _roomActionState.value = Resource.Loading
            val result = createRoomUseCase(
                hostId = profile.myPlayerId,
                hostName = profile.myNickname.ifBlank { "Host" },
                gameType = gameType,
                gridSize = gridSize
            )
            if (result.isSuccess) {
                _roomActionState.value = Resource.Success(result.getOrThrow())
            } else {
                _roomActionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Failed to create room"
                )
            }
        }
    }

    fun joinRoom(roomCode: String) {
        viewModelScope.launch {
            _roomActionState.value = Resource.Loading
            val result = joinRoomUseCase(
                roomCode = roomCode,
                partnerId = profile.myPlayerId,
                partnerName = profile.myNickname.ifBlank { "Player" }
            )
            if (result.isSuccess) {
                _roomActionState.value = Resource.Success(result.getOrThrow())
            } else {
                _roomActionState.value = Resource.Error(
                    result.exceptionOrNull()?.message ?: "Failed to join room"
                )
            }
        }
    }

    fun checkForOtaUpdates(currentVersion: String) {
        viewModelScope.launch {
            val result = checkOtaUpdateUseCase(currentVersion)
            if (result.isSuccess) {
                val updateInfo = result.getOrThrow()
                if (updateInfo.hasUpdate) {
                    _otaState.value = updateInfo
                }
            }
        }
    }

    fun resetActionState() {
        _roomActionState.value = Resource.Idle
    }
}
