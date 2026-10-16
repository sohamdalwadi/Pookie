package com.pookie.game.domain.model

enum class Gender(val id: String, val displayName: String) {
    MALE("MALE", "Male"),
    FEMALE("FEMALE", "Female");

    companion object {
        fun fromId(id: String?): Gender = entries.find { it.id.equals(id, ignoreCase = true) } ?: MALE
    }
}
