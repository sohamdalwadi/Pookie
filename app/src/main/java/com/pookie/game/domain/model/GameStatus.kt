package com.pookie.game.domain.model

enum class GameStatus(val raw: String) {
    WAITING("WAITING"),
    IN_PROGRESS("IN_PROGRESS"),
    GAME_OVER("GAME_OVER"),
    ABANDONED("ABANDONED");

    companion object {
        fun fromRaw(raw: String?): GameStatus = entries.find { it.raw.equals(raw, ignoreCase = true) } ?: WAITING
    }
}
