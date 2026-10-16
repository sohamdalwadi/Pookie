package com.pookie.game.domain.model

data class CoupleProfile(
    val myPlayerId: String,
    val myNickname: String,
    val gender: Gender = Gender.MALE,
    val partnerNickname: String = "",
    val vibeTag: String = "🍕 Foodies",
    val soundEnabled: Boolean = true
)
