package com.pookie.game.domain.model

data class SoulSyncState(
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val promptText: String = "What was the exact moment you knew you had feelings for me?",
    val phase: String = "ANSWERING", // "ANSWERING" or "JUDGING"
    val hostAnswer: String? = null,
    val partnerAnswer: String? = null,
    val hasHostAnswered: Boolean = false,
    val hasPartnerAnswered: Boolean = false,
    val answersRevealed: Boolean = false,
    val verdicts: Map<String, String?> = emptyMap(), // "host" -> "MATCH"|"MISS", "partner" -> ...
    val playerReady: Map<String, Boolean> = emptyMap(),
    val coupleScore: Int = 0,
    val lastRoundOutcome: String? = null,
    val completed: Boolean = false
) {
    fun hasPlayerAnswered(isHost: Boolean): Boolean {
        return if (isHost) {
            hostAnswer != null || hasHostAnswered
        } else {
            partnerAnswer != null || hasPartnerAnswered
        }
    }

    fun isPlayerReadyToJudge(isHost: Boolean): Boolean {
        val roleKey = if (isHost) "host" else "partner"
        return playerReady[roleKey] == true
    }

    fun getPlayerVerdict(isHost: Boolean): String? {
        val roleKey = if (isHost) "host" else "partner"
        return verdicts[roleKey]
    }
}
