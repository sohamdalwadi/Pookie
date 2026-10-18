package com.pookie.game.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pookie.game.domain.model.CoupleProfile
import com.pookie.game.domain.model.Gender
import com.pookie.game.domain.repository.IPreferencesRepository
import java.util.UUID

class PreferencesRepositoryImpl(
    context: Context
) : IPreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "pookie_game_prefs"
        private const val KEY_BACKEND_ID = "backend_deployment_id"
        private const val KEY_BACKEND_URL = "backend_custom_url"
        private const val KEY_MY_PLAYER_ID = "my_player_id"
        private const val KEY_MY_NICKNAME = "my_nickname"
        private const val KEY_GENDER = "gender"
        private const val KEY_PARTNER_NICKNAME = "partner_nickname"
        private const val KEY_VIBE_TAG = "vibe_tag"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
    }

    override fun getBackendDeploymentId(): String? =
        prefs.getString(KEY_BACKEND_ID, null)?.takeIf { it.isNotBlank() }

    override fun saveBackendDeploymentId(deploymentId: String) {
        val trimmed = deploymentId.trim()
        if (trimmed.isBlank()) {
            prefs.edit().remove(KEY_BACKEND_ID).apply()
        } else {
            prefs.edit()
                .putString(KEY_BACKEND_ID, trimmed)
                .remove(KEY_BACKEND_URL)
                .apply()
        }
    }

    override fun getBackendUrl(): String? =
        prefs.getString(KEY_BACKEND_URL, null)?.takeIf { it.isNotBlank() }

    override fun saveBackendUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            prefs.edit().remove(KEY_BACKEND_URL).apply()
        } else {
            prefs.edit()
                .putString(KEY_BACKEND_URL, trimmed)
                .remove(KEY_BACKEND_ID)
                .apply()
        }
    }

    override fun getCoupleProfile(): CoupleProfile {
        var myId = prefs.getString(KEY_MY_PLAYER_ID, null)
        if (myId.isNullOrBlank()) {
            myId = "P_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString(KEY_MY_PLAYER_ID, myId).apply()
        }

        return CoupleProfile(
            myPlayerId = myId,
            myNickname = prefs.getString(KEY_MY_NICKNAME, "") ?: "",
            gender = Gender.fromId(prefs.getString(KEY_GENDER, Gender.MALE.id)),
            partnerNickname = prefs.getString(KEY_PARTNER_NICKNAME, "") ?: "",
            vibeTag = prefs.getString(KEY_VIBE_TAG, "🍕 Foodies") ?: "🍕 Foodies",
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        )
    }

    override fun saveCoupleProfile(profile: CoupleProfile) {
        prefs.edit()
            .putString(KEY_MY_PLAYER_ID, profile.myPlayerId)
            .putString(KEY_MY_NICKNAME, profile.myNickname.trim())
            .putString(KEY_GENDER, profile.gender.id)
            .putString(KEY_PARTNER_NICKNAME, profile.partnerNickname.trim())
            .putString(KEY_VIBE_TAG, profile.vibeTag)
            .putBoolean(KEY_SOUND_ENABLED, profile.soundEnabled)
            .apply()
    }

    override fun isSoundEnabled(): Boolean =
        prefs.getBoolean(KEY_SOUND_ENABLED, true)

    override fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
}
