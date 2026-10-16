package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class SubmitVerdictUseCase(
    private val repository: IGameRepository
) {
    suspend operator fun invoke(roomCode: String, playerId: String, verdict: String): Result<Room> {
        return repository.submitVerdict(roomCode, playerId, verdict)
    }
}
