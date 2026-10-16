package com.pookie.game.data.model.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ApiResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("data") val data: JsonElement?,
    @SerializedName("error") val error: ApiErrorDto?
) {
    val isSuccess: Boolean get() = status.equals("SUCCESS", ignoreCase = true)
}

data class ApiErrorDto(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)

data class PlayerDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("score") val score: Int? = 0,
    @SerializedName("gender") val gender: String? = "MALE"
)

data class RematchStateDto(
    @SerializedName("hostReady") val hostReady: Boolean? = false,
    @SerializedName("partnerReady") val partnerReady: Boolean? = false
)

data class RoomDto(
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("gameType") val gameType: String,
    @SerializedName("host") val host: PlayerDto,
    @SerializedName("partner") val partner: PlayerDto?,
    @SerializedName("currentTurn") val currentTurn: String,
    @SerializedName("gameState") val gameState: JsonElement?,
    @SerializedName("rematchState") val rematchState: RematchStateDto?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("lastActivity") val lastActivity: String?,
    @SerializedName("version") val version: Int
)
