package com.example.practicando.entities

import kotlin.random.Random

data class Cloud(
    var x: Float,
    var y: Float,
    var width: Float = 70f,
    var height: Float = 30f,
    var speedFactor: Float = 0.25f
)

/**
 * Maneja el desplazamiento continuo del suelo y la decoración del fondo (nubes/estrellas).
 */
class Ground(
    var groundY: Float = 0f
) {
    var offset: Float = 0f
        private set

    val clouds = mutableListOf<Cloud>()

    /**
     * Inicializa nubes decorativas en el fondo.
     */
    fun initDecorations(screenWidth: Float, targetGroundY: Float) {
        groundY = targetGroundY
        clouds.clear()
        if (screenWidth > 0f) {
            // Crear de 3 a 5 nubes distribuidas
            val random = Random(42)
            for (i in 0..4) {
                clouds.add(
                    Cloud(
                        x = random.nextFloat() * screenWidth * 1.5f,
                        y = targetGroundY * (0.15f + random.nextFloat() * 0.35f)
                    )
                )
            }
        }
    }

    /**
     * Actualiza el desplazamiento del suelo y los elementos decorativos con efecto parallax.
     */
    fun update(deltaTime: Float, speed: Float, screenWidth: Float) {
        if (screenWidth <= 0f) return

        // Suelo continuo
        offset = (offset + speed * deltaTime) % screenWidth

        // Nubes en parallax (desplazamiento más lento)
        for (cloud in clouds) {
            cloud.x -= speed * cloud.speedFactor * deltaTime
            if (cloud.x + cloud.width < 0f) {
                cloud.x = screenWidth + Random.nextFloat() * (screenWidth * 0.5f)
            }
        }
    }

    /**
     * Reinicia el estado del suelo.
     */
    fun reset() {
        offset = 0f
    }
}
