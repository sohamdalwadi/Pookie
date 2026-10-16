package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.GameType
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class CreateRoomUseCase(private val gameRepository: IGameRepository) {
    suspend operator fun invoke(
        hostId: String,
        hostName: String,
        gameType: GameType,
        gridSize: Int = 3
    ): Result<Room> {
        if (hostName.isBlank()) {
            return Result.failure(IllegalArgumentException("Host name cannot be empty."))
        }
        return gameRepository.createRoom(hostId, hostName.trim(), gameType, gridSize)
    }
}
