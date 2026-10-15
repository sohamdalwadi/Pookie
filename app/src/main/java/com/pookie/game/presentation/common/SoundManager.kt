package com.pookie.game.presentation.common

import android.media.AudioManager
import android.media.ToneGenerator
import com.pookie.game.domain.repository.IPreferencesRepository

class SoundManager(
    private val preferencesRepository: IPreferencesRepository
) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playMoveSound() {
        if (!preferencesRepository.isSoundEnabled()) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        } catch (_: Exception) {}
    }

    fun playReactionSound() {
        if (!preferencesRepository.isSoundEnabled()) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (_: Exception) {}
    }

    fun playVictorySound() {
        if (!preferencesRepository.isSoundEnabled()) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
        } catch (_: Exception) {}
    }

    fun playDefeatSound() {
        if (!preferencesRepository.isSoundEnabled()) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 150)
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
