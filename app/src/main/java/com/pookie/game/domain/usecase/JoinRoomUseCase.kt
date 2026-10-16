package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class JoinRoomUseCase(private val gameRepository: IGameRepository) {
    suspend operator fun invoke(
        roomCode: String,
        partnerId: String,
        partnerName: String,
        gender: String = "MALE"
    ): Result<Room> {
        val sanitizedCode = roomCode.replace(Regex("[\\s-]"), "").uppercase()
        if (sanitizedCode.length < 4) {
            return Result.failure(IllegalArgumentException("Invalid room code."))
        }
        if (partnerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Partner name cannot be empty."))
        }
        return gameRepository.joinRoom(sanitizedCode, partnerId, partnerName.trim(), gender)
    }
}
