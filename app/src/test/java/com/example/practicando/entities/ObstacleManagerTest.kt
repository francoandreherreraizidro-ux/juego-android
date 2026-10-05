package com.example.practicando.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ObstacleManagerTest {

    private lateinit var manager: ObstacleManager

    @Before
    fun setUp() {
        manager = ObstacleManager()
        manager.reset()
    }

    @Test
    fun resetRestoresInitialSpeedAndClearsObstacles() {
        assertEquals(manager.baseSpeed, manager.currentSpeed, 0.01f)
        assertTrue(manager.activeObstacles.isEmpty())
        assertEquals(0f, manager.totalTimeElapsed, 0.01f)
    }

    @Test
    fun speedIncreasesProgressivelyWithTime() {
        manager.update(deltaTime = 2f, screenWidth = 1000f, groundY = 400f, playerStandHeight = 70f)

        val expectedSpeed = manager.baseSpeed + 2f * ObstacleManager.SPEED_ACCELERATION
        assertEquals(expectedSpeed, manager.currentSpeed, 0.01f)
    }

    @Test
    fun obstaclesMoveLeftAndAreRemovedWhenOffscreen() {
        val obstacle = Obstacle(
            x = 10f,
            y = 350f,
            width = 30f,
            height = 50f,
            type = ObstacleType.SMALL_CACTUS
        )
        manager.activeObstacles.add(obstacle)

        // Mover suficiente para que salga de la pantalla (-10 - 30 < 0)
        manager.update(deltaTime = 1f, screenWidth = 1000f, groundY = 400f, playerStandHeight = 70f)

        // Debe haber sido eliminado de activeObstacles
        assertTrue(manager.activeObstacles.none { it == obstacle })
    }

    @Test
    fun minimumGapConstraintIsRespected() {
        // Crear un obstáculo cerca del borde derecho
        val obstacle = Obstacle(
            x = 900f,
            y = 350f,
            width = 30f,
            height = 50f,
            type = ObstacleType.SMALL_CACTUS
        )
        manager.activeObstacles.add(obstacle)

        // Actualizar en un frame pequeño
        manager.update(deltaTime = 0.016f, screenWidth = 1000f, groundY = 400f, playerStandHeight = 70f)

        // Dado que el espacio libre es 1000 - (900 + 30) = 70px < MIN_OBSTACLE_GAP (380px), NO se debe haber generado otro obstáculo
        assertEquals(1, manager.activeObstacles.size)
    }
}
