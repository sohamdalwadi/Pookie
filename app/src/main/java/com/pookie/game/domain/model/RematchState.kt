package com.pookie.game.domain.model

data class RematchState(
    val hostReady: Boolean = false,
    val partnerReady: Boolean = false
) {
    val isBothReady: Boolean get() = hostReady && partnerReady
}
