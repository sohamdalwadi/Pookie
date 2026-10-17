package com.pookie.game.domain.usecase

import com.pookie.game.domain.repository.IGameRepository

class LeaveRoomUseCase(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(roomCode: String, playerId: String): Result<Boolean> =
        gameRepository.leaveRoom(roomCode, playerId)
}
