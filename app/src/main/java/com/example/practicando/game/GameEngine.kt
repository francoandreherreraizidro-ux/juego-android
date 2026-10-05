package com.example.practicando.game

import com.example.practicando.collision.CollisionManager
import com.example.practicando.entities.Ground
import com.example.practicando.entities.ObstacleManager
import com.example.practicando.entities.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Motor del juego: Coordinador central de la lógica, físicas, entidades y estados.
 *
 * Máquina de estados:
 * MENU -> PLAYING -> PAUSED -> PLAYING -> GAME_OVER -> MENU / PLAYING (Restart)
 */
class GameEngine(
    val player: Player = Player(),
    val ground: Ground = Ground(),
    val obstacleManager: ObstacleManager = ObstacleManager(),
    val collisionManager: CollisionManager = CollisionManager()
) {
    private val _state = MutableStateFlow(GameState.MENU)
    val state: StateFlow<GameState> = _state.asStateFlow()

    var screenWidth: Float = 0f
        private set

    var screenHeight: Float = 0f
        private set

    var groundY: Float = 0f
        private set

    var currentScore: Float = 0f
        private set

    var highScore: Int = 0
        private set

    // Eventos para efectos de sonido
    var onJumpListener: (() -> Unit)? = null
    var onGameOverListener: (() -> Unit)? = null
    var onScoreMilestoneListener: (() -> Unit)? = null

    private var lastScoreMilestone: Int = 0

    companion object {
        const val SCORE_RATE: Float = 0.025f // Puntos ganados en relación con la velocidad
    }

    /**
     * Inicializa las dimensiones de la pantalla y la línea base del suelo.
     */
    fun onScreenSizeChanged(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        screenWidth = width
        screenHeight = height
        groundY = height * 0.82f // Suelo ubicado al 82% de la altura de pantalla

        player.standWidth = height * 0.09f
        player.standHeight = height * 0.11f
        player.reset(startX = width * 0.12f, targetGroundY = groundY)

        ground.initDecorations(screenWidth = width, targetGroundY = groundY)
    }

    /**
     * Inicia una nueva partida desde el menú o reintenta tras Game Over.
     */
    fun startGame() {
        if (_state.value == GameState.MENU || _state.value == GameState.GAME_OVER) {
            resetGameState()
            _state.value = GameState.PLAYING
        }
    }

    /**
     * Pausa el juego activo.
     */
    fun pauseGame() {
        if (_state.value == GameState.PLAYING) {
            _state.value = GameState.PAUSED
        }
    }

    /**
     * Reanuda la partida desde la pausa.
     */
    fun resumeGame() {
        if (_state.value == GameState.PAUSED) {
            _state.value = GameState.PLAYING
        }
    }

    /**
     * Transición a Game Over tras una colisión.
     */
    fun gameOver() {
        if (_state.value == GameState.PLAYING) {
            _state.value = GameState.GAME_OVER
            if (currentScore.toInt() > highScore) {
                highScore = currentScore.toInt()
            }
            onGameOverListener?.invoke()
        }
    }

    /**
     * Reinicia la partida desde Pausa o Game Over.
     */
    fun restartGame() {
        if (_state.value == GameState.PAUSED || _state.value == GameState.GAME_OVER) {
            resetGameState()
            _state.value = GameState.PLAYING
        }
    }

    /**
     * Regresa al menú principal.
     */
    fun resetToMenu() {
        if (_state.value == GameState.PAUSED || _state.value == GameState.GAME_OVER) {
            _state.value = GameState.MENU
        }
    }

    /**
     * Acción de toque en la pantalla para saltar.
     */
    fun onTap() {
        if (_state.value == GameState.PLAYING) {
            val jumped = player.jump()
            if (jumped) {
                onJumpListener?.invoke()
            }
        }
    }

    /**
     * Acción de agachado.
     */
    fun onDuck(ducking: Boolean) {
        if (_state.value == GameState.PLAYING) {
            player.setDucking(ducking)
        }
    }

    /**
     * Sincroniza el High Score con la persistencia.
     */
    fun updateHighScore(savedHighScore: Int) {
        if (savedHighScore > highScore) {
            highScore = savedHighScore
        }
    }

    /**
     * Restablece la posición del jugador, obstáculos y puntuación.
     */
    private fun resetGameState() {
        currentScore = 0f
        lastScoreMilestone = 0
        player.reset(startX = screenWidth * 0.12f, targetGroundY = groundY)
        obstacleManager.reset()
        ground.reset()
    }

    /**
     * Actualiza la puntuación.
     */
    fun addScore(amount: Float) {
        if (_state.value == GameState.PLAYING) {
            currentScore += amount
            val currentInt = currentScore.toInt()

            // Reproducir sonido por cada 100 puntos alcanzados
            if (currentInt > 0 && currentInt % 100 == 0 && currentInt != lastScoreMilestone) {
                lastScoreMilestone = currentInt
                onScoreMilestoneListener?.invoke()
            }

            if (currentInt > highScore) {
                highScore = currentInt
            }
        }
    }

    /**
     * Bucle de actualización ejecutado en cada frame por GameLoop.
     */
    fun update(deltaTime: Float) {
        if (_state.value != GameState.PLAYING) return
        if (screenWidth <= 0f || screenHeight <= 0f) return

        // 1. Actualizar física del jugador
        player.update(deltaTime)

        // 2. Actualizar suelo y nubes
        ground.update(deltaTime, obstacleManager.currentSpeed, screenWidth)

        // 3. Actualizar obstáculos y dificultad
        obstacleManager.update(deltaTime, screenWidth, groundY, player.standHeight)

        // 4. Incrementar puntuación basada en distancia/velocidad
        addScore(obstacleManager.currentSpeed * SCORE_RATE * deltaTime)

        // 5. Verificar colisiones
        if (collisionManager.checkAnyCollision(player, obstacleManager.activeObstacles)) {
            gameOver()
        }
    }
}
