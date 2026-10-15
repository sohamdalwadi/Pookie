package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class GetGameStateUseCase(private val gameRepository: IGameRepository) {
    suspend operator fun invoke(roomCode: String, knownVersion: Int): Result<Room?> {
        val sanitizedCode = roomCode.replace(Regex("[\\s-]"), "").uppercase()
        return gameRepository.getGameState(sanitizedCode, knownVersion)
    }
}
