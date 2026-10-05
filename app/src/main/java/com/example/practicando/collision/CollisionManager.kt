package com.example.practicando.collision

import com.example.practicando.entities.Obstacle
import com.example.practicando.entities.Player

/**
 * Encargado exclusivamente de la detección de colisiones entre el jugador y los obstáculos.
 * Aplica el principio de responsabilidad única (SRP).
 */
class CollisionManager {

    /**
     * Evalúa si el jugador intersecta con un obstáculo específico.
     */
    fun checkCollision(player: Player, obstacle: Obstacle): Boolean {
        val playerBox = player.getHitbox()
        val obstacleBox = obstacle.getHitbox()
        return playerBox.overlaps(obstacleBox)
    }

    /**
     * Evalúa si el jugador colisiona con alguno de los obstáculos activos en la pantalla.
     */
    fun checkAnyCollision(player: Player, obstacles: List<Obstacle>): Boolean {
        for (i in obstacles.indices) {
            if (checkCollision(player, obstacles[i])) {
                return true
            }
        }
        return false
    }
}
