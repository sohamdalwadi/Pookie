package com.pookie.game.domain.model

data class Reaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val playerId: String,
    val type: ReactionType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ReactionType(val code: String, val drawableResName: String) {
    HEART("HEART", "ic_reaction_heart"),
    LIGHTNING("LIGHTNING", "ic_reaction_lightning"),
    CROWN("CROWN", "ic_reaction_crown");

    companion object {
        fun fromCode(code: String?): ReactionType = entries.find { it.code.equals(code, ignoreCase = true) } ?: HEART
    }
}
