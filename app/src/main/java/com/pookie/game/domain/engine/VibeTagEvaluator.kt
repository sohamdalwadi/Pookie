package com.pookie.game.domain.engine

import com.pookie.game.domain.model.GameType

/**
 * Dynamically awards couple vibe tags based on in-game activity and performance.
 */
object VibeTagEvaluator {

    fun evaluateTag(
        gameType: GameType,
        isVictory: Boolean,
        isDraw: Boolean,
        matchScore: Int,
        currentTag: String
    ): String {
        return when (gameType) {
            GameType.COUPLES_QUIZ -> {
                if (matchScore >= 4) "✨ Soulmates" else "🎬 Binge Buddies"
            }
            GameType.TIC_TAC_TOE -> {
                if (isDraw) "⚡ Drawbreakers" else "🍕 Foodies"
            }
            GameType.DOTS_AND_BOXES -> {
                if (isVictory) "👑 Territory Masters" else "🎮 Gamers"
            }
        }
    }
}
