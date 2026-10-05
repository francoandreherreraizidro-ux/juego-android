package com.example.practicando.entities

import kotlin.random.Random

/**
 * Gestor y generador controlado de obstáculos.
 * Optimizado para rendimiento de 60 FPS sin asignaciones innecesarias de objetos por cuadro.
 */
class ObstacleManager {

    val activeObstacles = mutableListOf<Obstacle>()

    var baseSpeed: Float = 450f          // Velocidad inicial (px/s)
    var maxSpeed: Float = 1000f          // Velocidad máxima
    var currentSpeed: Float = baseSpeed  // Velocidad actual del juego
        private set

    var totalTimeElapsed: Float = 0f
        private set

    private var spawnTimer: Float = 0f
    private var nextSpawnDelay: Float = 1.8f
    private val random = Random(System.currentTimeMillis())

    companion object {
        const val MIN_OBSTACLE_GAP: Float = 380f // Distancia mínima en px entre obstáculos
        const val SPEED_ACCELERATION: Float = 12f // Incremento de px/s por cada segundo transcurrido
    }

    /**
     * Reinicia la lista de obstáculos y restablece la velocidad de juego inicial.
     */
    fun reset() {
        activeObstacles.clear()
        currentSpeed = baseSpeed
        totalTimeElapsed = 0f
        spawnTimer = 0f
        nextSpawnDelay = 1.5f
    }

    /**
     * Actualiza la posición de los obstáculos, remueve los fuera de pantalla sin asignación de iteradores,
     * incrementa la dificultad y genera nuevos obstáculos respetando el margen de seguridad.
     */
    fun update(deltaTime: Float, screenWidth: Float, groundY: Float, playerStandHeight: Float) {
        if (screenWidth <= 0f || groundY <= 0f) return

        totalTimeElapsed += deltaTime

        // Incremento progresivo de velocidad
        currentSpeed = (baseSpeed + totalTimeElapsed * SPEED_ACCELERATION).coerceAtMost(maxSpeed)

        // 1. Mover obstáculos existentes y eliminar los que salgan (Loop directo por índice, 0 allocations)
        for (i in activeObstacles.indices.reversed()) {
            val obstacle = activeObstacles[i]
            obstacle.update(deltaTime, currentSpeed)
            if (obstacle.isOffscreen()) {
                activeObstacles.removeAt(i)
            }
        }

        // 2. Control de temporización y distancia para la generación de nuevos obstáculos
        spawnTimer += deltaTime

        val lastObstacle = activeObstacles.lastOrNull()
        val spaceSinceLast = if (lastObstacle != null) {
            screenWidth - (lastObstacle.x + lastObstacle.width)
        } else {
            Float.MAX_VALUE
        }

        // Solo generar si ha pasado el tiempo mínimo Y hay suficiente espacio físico libre
        if (spawnTimer >= nextSpawnDelay && spaceSinceLast >= MIN_OBSTACLE_GAP) {
            spawnObstacle(screenWidth, groundY, playerStandHeight)
            spawnTimer = 0f
            val minDelay = (1.1f - (currentSpeed - baseSpeed) / 1000f).coerceAtLeast(0.8f)
            val maxDelay = minDelay + 1.2f
            nextSpawnDelay = minDelay + random.nextFloat() * (maxDelay - minDelay)
        }
    }

    /**
     * Fabrica y posiciona un obstáculo nuevo al borde derecho de la pantalla.
     */
    private fun spawnObstacle(screenWidth: Float, groundY: Float, playerStandHeight: Float) {
        val allowBirds = totalTimeElapsed > 8f // Aparecen pájaros tras 8 segundos de juego
        val randType = if (allowBirds && random.nextFloat() < 0.35f) {
            ObstacleType.BIRD
        } else if (random.nextFloat() < 0.5f) {
            ObstacleType.SMALL_CACTUS
        } else {
            ObstacleType.LARGE_CACTUS
        }

        val count = if (randType != ObstacleType.BIRD) random.nextInt(1, 4) else 1

        val obstacle = when (randType) {
            ObstacleType.SMALL_CACTUS -> {
                val h = 50f
                val w = 30f * count
                Obstacle(
                    x = screenWidth,
                    y = groundY - h,
                    width = w,
                    height = h,
                    type = ObstacleType.SMALL_CACTUS,
                    count = count
                )
            }
            ObstacleType.LARGE_CACTUS -> {
                val h = 70f
                val w = 40f * count
                Obstacle(
                    x = screenWidth,
                    y = groundY - h,
                    width = w,
                    height = h,
                    type = ObstacleType.LARGE_CACTUS,
                    count = count
                )
            }
            ObstacleType.BIRD -> {
                val h = 40f
                val w = 55f
                // Alturas de vuelo fijas para evitar listas temporales en heap
                val flyingY = when (random.nextInt(3)) {
                    0 -> groundY - playerStandHeight * 0.4f  // Bajo (salto requerido)
                    1 -> groundY - playerStandHeight * 1.1f  // Medio (agachado requerido)
                    else -> groundY - playerStandHeight * 1.7f // Alto (se pasa corriendo)
                }
                Obstacle(
                    x = screenWidth,
                    y = flyingY,
                    width = w,
                    height = h,
                    type = ObstacleType.BIRD,
                    count = 1
                )
            }
        }

        activeObstacles.add(obstacle)
    }
}
