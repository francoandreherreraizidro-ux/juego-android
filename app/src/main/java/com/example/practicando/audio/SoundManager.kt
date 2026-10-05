package com.example.practicando.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * Encargado de la reproducción de efectos de sonido de baja latencia con SoundPool.
 * Sigue el principio de responsabilidad única (SRP).
 */
class SoundManager(private val context: Context) {

    var soundEnabled: Boolean = true

    private var soundPool: SoundPool? = null
    private var jumpSoundId: Int = 0
    private var scoreSoundId: Int = 0
    private var collisionSoundId: Int = 0

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            val jumpResId = context.resources.getIdentifier("jump", "raw", context.packageName)
            if (jumpResId != 0) {
                jumpSoundId = soundPool?.load(context, jumpResId, 1) ?: 0
            }

            val scoreResId = context.resources.getIdentifier("score", "raw", context.packageName)
            if (scoreResId != 0) {
                scoreSoundId = soundPool?.load(context, scoreResId, 1) ?: 0
            }

            val hitResId = context.resources.getIdentifier("hit", "raw", context.packageName)
            if (hitResId != 0) {
                collisionSoundId = soundPool?.load(context, hitResId, 1) ?: 0
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error al inicializar SoundPool", e)
        }
    }

    fun playJump() {
        if (!soundEnabled) return
        if (jumpSoundId != 0) {
            soundPool?.play(jumpSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playScore() {
        if (!soundEnabled) return
        if (scoreSoundId != 0) {
            soundPool?.play(scoreSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playCollision() {
        if (!soundEnabled) return
        if (collisionSoundId != 0) {
            soundPool?.play(collisionSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
