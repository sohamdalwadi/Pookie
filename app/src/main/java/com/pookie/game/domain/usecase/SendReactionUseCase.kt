package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.ReactionType
import com.pookie.game.domain.repository.IGameRepository

class SendReactionUseCase(private val gameRepository: IGameRepository) {
    suspend operator fun invoke(roomCode: String, playerId: String, reaction: ReactionType): Result<Boolean> {
        val sanitizedCode = roomCode.replace(Regex("[\\s-]"), "").uppercase()
        return gameRepository.sendReaction(sanitizedCode, playerId, reaction)
    }
}
