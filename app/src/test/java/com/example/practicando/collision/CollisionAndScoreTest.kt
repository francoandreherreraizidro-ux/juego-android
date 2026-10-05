package com.example.practicando.collision

import com.example.practicando.entities.Obstacle
import com.example.practicando.entities.ObstacleType
import com.example.practicando.game.GameEngine
import com.example.practicando.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CollisionAndScoreTest {

    private lateinit var gameEngine: GameEngine

    @Before
    fun setUp() {
        gameEngine = GameEngine()
        gameEngine.onScreenSizeChanged(width = 1000f, height = 500f)
    }

    @Test
    fun collisionDetectedWhenPlayerOverlapsObstacle() {
        val player = gameEngine.player
        val obstacle = Obstacle(
            x = player.x + 5f,
            y = player.y + 5f,
            width = 30f,
            height = 50f,
            type = ObstacleType.SMALL_CACTUS
        )

        val collision = gameEngine.collisionManager.checkCollision(player, obstacle)
        assertTrue(collision)
    }

    @Test
    fun noCollisionWhenObstacleIsFarAway() {
        val player = gameEngine.player
        val obstacle = Obstacle(
            x = player.x + 500f,
            y = player.y,
            width = 30f,
            height = 50f,
            type = ObstacleType.SMALL_CACTUS
        )

        val collision = gameEngine.collisionManager.checkCollision(player, obstacle)
        assertFalse(collision)
    }

    @Test
    fun collisionTriggersGameOverInGameEngine() {
        gameEngine.startGame()
        assertEquals(GameState.PLAYING, gameEngine.state.value)

        // Colocar un obstáculo directamente sobre el jugador
        val obstacle = Obstacle(
            x = gameEngine.player.x,
            y = gameEngine.player.y,
            width = 30f,
            height = 50f,
            type = ObstacleType.SMALL_CACTUS
        )
        gameEngine.obstacleManager.activeObstacles.add(obstacle)

        // Ejecutar frame
        gameEngine.update(0.016f)

        assertEquals(GameState.GAME_OVER, gameEngine.state.value)
    }

    @Test
    fun scoreIncreasesAndUpdatesHighScoreDuringPlay() {
        gameEngine.startGame()
        val initialScore = gameEngine.currentScore

        // Simular 1 segundo de juego
        gameEngine.update(1.0f)

        assertTrue(gameEngine.currentScore > initialScore)
        assertEquals(gameEngine.currentScore.toInt(), gameEngine.highScore)
    }
}
