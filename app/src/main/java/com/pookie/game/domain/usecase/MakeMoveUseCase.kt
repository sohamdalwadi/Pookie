package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.model.Room
import com.pookie.game.domain.repository.IGameRepository

class MakeMoveUseCase(private val gameRepository: IGameRepository) {
    suspend operator fun invoke(
        roomCode: String,
        playerId: String,
        move: Any,
        clientVersion: Int,
        piggybackReaction: ReactionType? = null
    ): Result<Room> {
        val sanitizedCode = roomCode.replace(Regex("[\\s-]"), "").uppercase()
        return gameRepository.makeMove(sanitizedCode, playerId, move, clientVersion, piggybackReaction)
    }
}
