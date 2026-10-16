package com.pookie.game.domain.model

enum class GameType(val id: String, val displayName: String) {
    TIC_TAC_TOE("TIC_TAC_TOE", "Tic-Tac-Toe"),
    DOTS_AND_BOXES("DOTS_AND_BOXES", "Dots & Boxes"),
    COUPLES_QUIZ("COUPLES_QUIZ", "Couples Quiz"),
    SOUL_SYNC("SOUL_SYNC", "SoulSync");

    companion object {
        fun fromId(id: String?): GameType = entries.find { it.id.equals(id, ignoreCase = true) } ?: TIC_TAC_TOE
    }
}
