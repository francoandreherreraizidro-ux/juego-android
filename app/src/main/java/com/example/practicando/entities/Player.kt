package com.example.practicando.entities

import androidx.compose.ui.geometry.Rect

/**
 * Representa al jugador (Dinosaurio) en el juego.
 * Maneja la posición, física de salto, gravedad, agachado y animación.
 */
class Player(
    var x: Float = 100f,
    var y: Float = 0f,
    var standWidth: Float = 60f,
    var standHeight: Float = 70f
) {
    var width: Float = standWidth
    var height: Float = standHeight

    var velocityY: Float = 0f
    var isJumping: Boolean = false
        private set

    var isDucking: Boolean = false
        private set

    var groundY: Float = 0f

    // Estado para animación de piernas al correr
    var runFrame: Int = 0
        private set
    private var animTimer: Float = 0f

    companion object {
        const val GRAVITY: Float = 2400f        // Fuerza de gravedad (px/s²)
        const val JUMP_VELOCITY: Float = -900f  // Impulso inicial de salto (hacia arriba, Y negativo)
        const val ANIM_FRAME_DURATION: Float = 0.1f // Tiempo por cuadro de animación (100ms)
    }

    /**
     * Inicializa o reinicia la posición del dinosaurio en el suelo especificado.
     */
    fun reset(startX: Float, targetGroundY: Float) {
        x = startX
        groundY = targetGroundY
        isJumping = false
        isDucking = false
        velocityY = 0f
        width = standWidth
        height = standHeight
        y = groundY - height
        runFrame = 0
        animTimer = 0f
    }

    /**
     * Ejecuta el salto si el dinosaurio está apoyado sobre el suelo.
     * Previene saltos infinitos en el aire.
     */
    fun jump(): Boolean {
        if (!isJumping && !isDucking) {
            velocityY = JUMP_VELOCITY
            isJumping = true
            return true
        }
        return false
    }

    /**
     * Alterna el estado de agachado (útil para esquivar pterodáctilos).
     */
    fun setDucking(ducking: Boolean) {
        if (isJumping) return // No se puede agachar en el aire
        if (isDucking != ducking) {
            isDucking = ducking
            if (isDucking) {
                height = standHeight * 0.55f
                width = standWidth * 1.2f
            } else {
                height = standHeight
                width = standWidth
            }
            // Reposicionar Y acorde al nuevo tamaño
            y = groundY - height
        }
    }

    /**
     * Actualiza la física de posición, gravedad y ciclo de animación mediante deltaTime.
     */
    fun update(deltaTime: Float) {
        // Aplicar gravedad y velocidad vertical
        if (isJumping || y < groundY - height) {
            velocityY += GRAVITY * deltaTime
            y += velocityY * deltaTime

            // Detección de colisión con el suelo
            if (y >= groundY - height) {
                y = groundY - height
                velocityY = 0f
                isJumping = false
            }
        } else {
            // Ciclo de animación de piernas al correr en el suelo
            animTimer += deltaTime
            if (animTimer >= ANIM_FRAME_DURATION) {
                animTimer = 0f
                runFrame = (runFrame + 1) % 2
            }
        }
    }

    /**
     * Obtiene el rectángulo de colisión (Hitbox) del jugador.
     * Incluye un margen interno (padding) para lograr una detección de colisión justa y precisa.
     */
    fun getHitbox(): Rect {
        val insetX = width * 0.1f
        val insetY = height * 0.08f
        return Rect(
            x + insetX,
            y + insetY,
            x + width - insetX,
            y + height - insetY
        )
    }
}
