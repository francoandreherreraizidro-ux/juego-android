package com.example.practicando.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.practicando.entities.Ground
import com.example.practicando.entities.Obstacle
import com.example.practicando.entities.ObstacleType
import com.example.practicando.entities.Player

val DINO_COLOR = Color(0xFF535353)
val GROUND_COLOR = Color(0xFF535353)
val CACTUS_COLOR = Color(0xFF388E3C)
val BIRD_COLOR = Color(0xFF535353)
val CLOUD_COLOR = Color(0xFFD0D0D0)

// Instancias reusables de Path para evitar asignación de memoria (Garbage Collection) en cada frame
private val cachedTailPath = Path()
private val cachedHeadPath = Path()
private val cachedWingPath = Path()

/**
 * Renderiza el escenario del suelo y las nubes con cero asignación en el loop.
 */
fun DrawScope.drawGround(ground: Ground, screenWidth: Float) {
    if (ground.groundY <= 0f) return

    // 1. Línea principal del suelo
    drawLine(
        color = GROUND_COLOR,
        start = Offset(0f, ground.groundY),
        end = Offset(screenWidth, ground.groundY),
        strokeWidth = 3f
    )

    // 2. Pequeños detalles/puntos en el suelo desplazándose
    val dotStep = 60f
    var startDotX = -ground.offset % dotStep
    while (startDotX < screenWidth) {
        if (startDotX >= 0f) {
            drawCircle(
                color = GROUND_COLOR,
                radius = 2f,
                center = Offset(startDotX, ground.groundY + 8f)
            )
            drawCircle(
                color = GROUND_COLOR,
                radius = 1.5f,
                center = Offset(startDotX + 25f, ground.groundY + 16f)
            )
        }
        startDotX += dotStep
    }

    // 3. Nubes decorativas en el fondo
    val clouds = ground.clouds
    for (i in clouds.indices) {
        val cloud = clouds[i]
        drawRoundRect(
            color = CLOUD_COLOR,
            topLeft = Offset(cloud.x, cloud.y),
            size = Size(cloud.width, cloud.height),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

/**
 * Renderiza al jugador (Dinosaurio) optimizado para 60 FPS con animación fluida.
 */
fun DrawScope.drawPlayer(player: Player) {
    if (player.width <= 0f || player.height <= 0f) return

    val px = player.x
    val py = player.y
    val pw = player.width
    val ph = player.height

    if (player.isDucking) {
        // Cuerpo agachado (horizontal)
        drawRoundRect(
            color = DINO_COLOR,
            topLeft = Offset(px, py + ph * 0.2f),
            size = Size(pw * 0.85f, ph * 0.75f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Cabeza sobresaliente a la derecha
        drawRoundRect(
            color = DINO_COLOR,
            topLeft = Offset(px + pw * 0.65f, py),
            size = Size(pw * 0.35f, ph * 0.6f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Ojo
        drawCircle(
            color = Color.White,
            radius = ph * 0.08f,
            center = Offset(px + pw * 0.8f, py + ph * 0.2f)
        )
    } else {
        // Cuerpo erguido
        drawRoundRect(
            color = DINO_COLOR,
            topLeft = Offset(px, py + ph * 0.3f),
            size = Size(pw * 0.75f, ph * 0.5f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Cabeza
        drawRoundRect(
            color = DINO_COLOR,
            topLeft = Offset(px + pw * 0.35f, py),
            size = Size(pw * 0.65f, ph * 0.35f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Ojo
        drawCircle(
            color = Color.White,
            radius = ph * 0.05f,
            center = Offset(px + pw * 0.75f, py + ph * 0.1f)
        )

        // Cola (utilizando Path reutilizable)
        cachedTailPath.reset()
        cachedTailPath.moveTo(px, py + ph * 0.4f)
        cachedTailPath.lineTo(px - pw * 0.2f, py + ph * 0.3f)
        cachedTailPath.lineTo(px, py + ph * 0.6f)
        cachedTailPath.close()
        drawPath(path = cachedTailPath, color = DINO_COLOR)

        // Piernas (Animación según runFrame)
        if (player.isJumping) {
            // Piernas recogidas en salto
            drawRect(
                color = DINO_COLOR,
                topLeft = Offset(px + pw * 0.25f, py + ph * 0.8f),
                size = Size(pw * 0.15f, ph * 0.2f)
            )
            drawRect(
                color = DINO_COLOR,
                topLeft = Offset(px + pw * 0.55f, py + ph * 0.8f),
                size = Size(pw * 0.15f, ph * 0.2f)
            )
        } else {
            // Piernas corriendo alternadamente
            if (player.runFrame == 0) {
                drawRect(
                    color = DINO_COLOR,
                    topLeft = Offset(px + pw * 0.2f, py + ph * 0.8f),
                    size = Size(pw * 0.15f, ph * 0.2f)
                )
                drawRect(
                    color = DINO_COLOR,
                    topLeft = Offset(px + pw * 0.55f, py + ph * 0.8f),
                    size = Size(pw * 0.15f, ph * 0.12f)
                )
            } else {
                drawRect(
                    color = DINO_COLOR,
                    topLeft = Offset(px + pw * 0.2f, py + ph * 0.8f),
                    size = Size(pw * 0.15f, ph * 0.12f)
                )
                drawRect(
                    color = DINO_COLOR,
                    topLeft = Offset(px + pw * 0.55f, py + ph * 0.8f),
                    size = Size(pw * 0.15f, ph * 0.2f)
                )
            }
        }
    }
}

/**
 * Renderiza obstáculos (Cactus o Pterodáctilos) reutilizando objetos de dibujo.
 */
fun DrawScope.drawObstacle(obstacle: Obstacle) {
    val ox = obstacle.x
    val oy = obstacle.y
    val ow = obstacle.width
    val oh = obstacle.height

    when (obstacle.type) {
        ObstacleType.SMALL_CACTUS, ObstacleType.LARGE_CACTUS -> {
            val cactusWidth = ow / obstacle.count
            for (i in 0 until obstacle.count) {
                val cx = ox + i * cactusWidth
                // Tronco principal
                drawRoundRect(
                    color = CACTUS_COLOR,
                    topLeft = Offset(cx + cactusWidth * 0.3f, oy),
                    size = Size(cactusWidth * 0.4f, oh),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Brazo izquierdo
                drawRoundRect(
                    color = CACTUS_COLOR,
                    topLeft = Offset(cx + cactusWidth * 0.1f, oy + oh * 0.3f),
                    size = Size(cactusWidth * 0.25f, oh * 0.35f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Brazo derecho
                drawRoundRect(
                    color = CACTUS_COLOR,
                    topLeft = Offset(cx + cactusWidth * 0.65f, oy + oh * 0.25f),
                    size = Size(cactusWidth * 0.25f, oh * 0.35f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
        ObstacleType.BIRD -> {
            // Cuerpo del Pterodáctilo
            drawRoundRect(
                color = BIRD_COLOR,
                topLeft = Offset(ox + ow * 0.2f, oy + oh * 0.35f),
                size = Size(ow * 0.6f, oh * 0.35f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Cabeza y pico (reutilizando cachedHeadPath)
            cachedHeadPath.reset()
            cachedHeadPath.moveTo(ox + ow * 0.2f, oy + oh * 0.35f)
            cachedHeadPath.lineTo(ox, oy + oh * 0.4f)
            cachedHeadPath.lineTo(ox + ow * 0.2f, oy + oh * 0.55f)
            cachedHeadPath.close()
            drawPath(path = cachedHeadPath, color = BIRD_COLOR)

            // Alas con animación (reutilizando cachedWingPath)
            cachedWingPath.reset()
            if (obstacle.wingFrame == 0) {
                // Ala hacia arriba
                cachedWingPath.moveTo(ox + ow * 0.4f, oy + oh * 0.35f)
                cachedWingPath.lineTo(ox + ow * 0.6f, oy)
                cachedWingPath.lineTo(ox + ow * 0.7f, oy + oh * 0.35f)
                cachedWingPath.close()
            } else {
                // Ala hacia abajo
                cachedWingPath.moveTo(ox + ow * 0.4f, oy + oh * 0.6f)
                cachedWingPath.lineTo(ox + ow * 0.6f, oy + oh)
                cachedWingPath.lineTo(ox + ow * 0.7f, oy + oh * 0.6f)
                cachedWingPath.close()
            }
            drawPath(path = cachedWingPath, color = BIRD_COLOR)
        }
    }
}
