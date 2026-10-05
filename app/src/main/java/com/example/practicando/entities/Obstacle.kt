package com.example.practicando.entities

import androidx.compose.ui.geometry.Rect

enum class ObstacleType {
    SMALL_CACTUS,
    LARGE_CACTUS,
    BIRD
}

/**
 * Representa un obstáculo (Cactus o Pterodáctilo) que se desplaza hacia el jugador.
 */
class Obstacle(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    val type: ObstacleType,
    val count: Int = 1 // Cantidad de cactus agrupados (1 a 3)
) {
    var wingFrame: Int = 0
        private set
    private var animTimer: Float = 0f

    /**
     * Actualiza la posición y la animación del obstáculo mediante deltaTime.
     */
    fun update(deltaTime: Float, speed: Float) {
        x -= speed * deltaTime

        // Animación de alas para el Pterodáctilo (BIRD)
        if (type == ObstacleType.BIRD) {
            animTimer += deltaTime
            if (animTimer >= 0.15f) {
                animTimer = 0f
                wingFrame = (wingFrame + 1) % 2
            }
        }
    }

    /**
     * Indica si el obstáculo ha salido completamente de la pantalla por la izquierda.
     */
    fun isOffscreen(): Boolean {
        return x + width < 0f
    }

    /**
     * Obtiene la Hitbox ajustada del obstáculo para detección de colisiones justa.
     */
    fun getHitbox(): Rect {
        val insetX = width * 0.12f
        val insetY = height * 0.10f
        return Rect(
            x + insetX,
            y + insetY,
            x + width - insetX,
            y + height - insetY
        )
    }
}
