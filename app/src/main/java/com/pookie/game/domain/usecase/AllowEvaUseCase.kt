package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class AllowEvaUseCase(
    private val gameRepository: IGameRepository
) {
    suspend operator fun invoke(roomCode: String): Result<Room> =
        gameRepository.allowEva(roomCode)
}
