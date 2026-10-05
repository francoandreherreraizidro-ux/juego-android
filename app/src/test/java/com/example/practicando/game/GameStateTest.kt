package com.example.practicando.game

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GameStateTest {

    private lateinit var gameEngine: GameEngine

    @Before
    fun setUp() {
        gameEngine = GameEngine()
    }

    @Test
    fun initialStateIsMenu() {
        assertEquals(GameState.MENU, gameEngine.state.value)
    }

    @Test
    fun startGameTransitionsToPlaying() {
        gameEngine.startGame()
        assertEquals(GameState.PLAYING, gameEngine.state.value)
    }

    @Test
    fun pauseGameTransitionsFromPlayingToPaused() {
        gameEngine.startGame()
        gameEngine.pauseGame()
        assertEquals(GameState.PAUSED, gameEngine.state.value)
    }

    @Test
    fun resumeGameTransitionsFromPausedToPlaying() {
        gameEngine.startGame()
        gameEngine.pauseGame()
        gameEngine.resumeGame()
        assertEquals(GameState.PLAYING, gameEngine.state.value)
    }

    @Test
    fun gameOverTransitionsFromPlayingToGameOver() {
        gameEngine.startGame()
        gameEngine.gameOver()
        assertEquals(GameState.GAME_OVER, gameEngine.state.value)
    }

    @Test
    fun restartGameFromGameOverResetsToPlaying() {
        gameEngine.startGame()
        gameEngine.addScore(100f)
        gameEngine.gameOver()
        assertEquals(GameState.GAME_OVER, gameEngine.state.value)

        gameEngine.restartGame()
        assertEquals(GameState.PLAYING, gameEngine.state.value)
        assertEquals(0f, gameEngine.currentScore, 0.01f)
    }

    @Test
    fun resetToMenuFromPausedReturnsToMenu() {
        gameEngine.startGame()
        gameEngine.pauseGame()
        gameEngine.resetToMenu()
        assertEquals(GameState.MENU, gameEngine.state.value)
    }

    @Test
    fun invalidPauseFromMenuDoesNothing() {
        gameEngine.pauseGame()
        assertEquals(GameState.MENU, gameEngine.state.value)
    }
}
