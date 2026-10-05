package com.example.practicando.game

/**
 * Representa la máquina de estados principal del juego.
 * Evita el uso de múltiples variables booleanas dispersas.
 */
enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}
