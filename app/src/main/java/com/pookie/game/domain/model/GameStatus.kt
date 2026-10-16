package com.pookie.game.domain.model

enum class GameStatus(val raw: String) {
    WAITING("WAITING"),
    IN_PROGRESS("IN_PROGRESS"),
    GAME_OVER("GAME_OVER"),
    ABANDONED("ABANDONED");

    companion object {
        fun fromRaw(raw: String?): GameStatus = when (raw?.uppercase()?.trim()) {
            "WAITING", "WAITING_FOR_PARTNER", "WAITING_FOR_PLAYERS" -> WAITING
            "IN_PROGRESS", "PLAYING" -> IN_PROGRESS
            "GAME_OVER", "FINISHED" -> GAME_OVER
            "ABANDONED" -> ABANDONED
            else -> WAITING
        }
    }
}
