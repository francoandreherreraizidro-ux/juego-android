package com.example.practicando.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.practicando.audio.SoundManager
import com.example.practicando.data.GameRepository
import com.example.practicando.game.GameEngine
import com.example.practicando.game.GameState
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { GameRepository(context) }
    val gameEngine = remember { GameEngine() }
    val soundManager = remember(context) { SoundManager(context) }

    val gameState by gameEngine.state.collectAsState()
    val savedHighScore by repository.highScoreFlow.collectAsState(initial = 0)
    val savedSoundEnabled by repository.soundEnabledFlow.collectAsState(initial = true)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Cargar y sincronizar datos guardados de DataStore
    LaunchedEffect(savedHighScore) {
        gameEngine.updateHighScore(savedHighScore)
    }

    LaunchedEffect(savedSoundEnabled) {
        soundManager.soundEnabled = savedSoundEnabled
    }

    // Guardar High Score en DataStore cuando el jugador supere el récord
    LaunchedEffect(gameEngine.highScore) {
        if (gameEngine.highScore > savedHighScore) {
            repository.saveHighScore(gameEngine.highScore)
        }
    }

    // Vincular listeners de efectos de sonido
    LaunchedEffect(soundManager) {
        gameEngine.onJumpListener = { soundManager.playJump() }
        gameEngine.onGameOverListener = { soundManager.playCollision() }
        gameEngine.onScoreMilestoneListener = { soundManager.playScore() }
    }

    // Interceptar ciclo de vida de Android para pausar el juego al ocultar la app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                gameEngine.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundManager.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (gameState) {
            GameState.MENU -> {
                MenuScreen(
                    highScore = gameEngine.highScore,
                    soundEnabled = soundManager.soundEnabled,
                    onStartGame = {
                        gameEngine.startGame()
                    },
                    onToggleSound = {
                        val newSoundState = !soundManager.soundEnabled
                        soundManager.soundEnabled = newSoundState
                        scope.launch {
                            repository.saveSoundEnabled(newSoundState)
                        }
                    }
                )
            }
            GameState.PLAYING, GameState.PAUSED, GameState.GAME_OVER -> {
                GameScreen(
                    gameEngine = gameEngine,
                    onPauseClicked = {
                        gameEngine.pauseGame()
                    }
                )

                if (gameState == GameState.PAUSED) {
                    PauseOverlay(
                        onResume = { gameEngine.resumeGame() },
                        onRestart = { gameEngine.restartGame() },
                        onMenu = { gameEngine.resetToMenu() }
                    )
                }

                if (gameState == GameState.GAME_OVER) {
                    GameOverOverlay(
                        currentScore = gameEngine.currentScore.toInt(),
                        highScore = gameEngine.highScore,
                        onRestart = { gameEngine.restartGame() },
                        onMenu = { gameEngine.resetToMenu() }
                    )
                }
            }
        }
    }
}
