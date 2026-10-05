package com.example.practicando.game

import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Bucle del juego impulsado por el VSync de la pantalla mediante [withFrameNanos].
 *
 * Desacopla la lógica de actualización física de los FPS del dispositivo pasando
 * un [deltaTime] (en segundos) en cada frame.
 */
class GameLoop(
    private val onUpdate: (deltaTime: Float) -> Unit
) {
    var isRunning: Boolean = false
        private set

    private var loopJob: Job? = null
    private var lastFrameTimeNanos: Long = 0L

    companion object {
        // Cota máxima para deltaTime (50ms = 0.05s, equivalente a ~20 FPS mínimos).
        // Evita saltos físicos exagerados tras pausas o caídas bruscas de frames.
        const val MAX_DELTA_TIME: Float = 0.05f

        /**
         * Calcula y limita el deltaTime entre dos marcas de tiempo en nanosegundos.
         */
        fun calculateDeltaTime(
            currentNanos: Long,
            previousNanos: Long,
            maxDelta: Float = MAX_DELTA_TIME
        ): Float {
            if (previousNanos <= 0L || currentNanos <= previousNanos) return 0f
            val rawDelta = (currentNanos - previousNanos) / 1_000_000_000f
            return rawDelta.coerceAtMost(maxDelta)
        }
    }

    /**
     * Inicia la ejecución del GameLoop dentro del [coroutineScope] especificado.
     */
    fun start(coroutineScope: CoroutineScope) {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0L

        loopJob = coroutineScope.launch {
            while (isActive && isRunning) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos == 0L) {
                        lastFrameTimeNanos = frameTimeNanos
                        return@withFrameNanos
                    }

                    val deltaTime = calculateDeltaTime(frameTimeNanos, lastFrameTimeNanos)
                    lastFrameTimeNanos = frameTimeNanos

                    if (deltaTime > 0f) {
                        onUpdate(deltaTime)
                    }
                }
            }
        }
    }

    /**
     * Pausa la ejecución del GameLoop y cancela la corrutina activa.
     */
    fun pause() {
        isRunning = false
        loopJob?.cancel()
        loopJob = null
        lastFrameTimeNanos = 0L
    }

    /**
     * Detiene por completo el GameLoop.
     */
    fun stop() {
        pause()
    }
}
