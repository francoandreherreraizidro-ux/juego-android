package com.example.practicando.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practicando.game.GameEngine
import com.example.practicando.game.GameLoop
import java.util.Locale

@Composable
fun GameScreen(
    gameEngine: GameEngine,
    onPauseClicked: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Estado para notificar a Compose la invalidación y redibujado del Canvas en cada frame
    var frameTick by remember { mutableLongStateOf(0L) }

    // Manejo del GameLoop desacoplado con la VSync de pantalla
    val gameLoop = remember {
        GameLoop { deltaTime ->
            gameEngine.update(deltaTime)
            frameTick = System.nanoTime() // Dispara la invalidación del Canvas
        }
    }

    LaunchedEffect(Unit) {
        gameLoop.start(coroutineScope)
    }

    DisposableEffect(Unit) {
        onDispose {
            gameLoop.stop()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Sincronizar dimensiones cuando la pantalla esté disponible
        LaunchedEffect(width, height) {
            gameEngine.onScreenSizeChanged(width, height)
        }

        // Canvas principal de dibujo 2D
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            gameEngine.onTap()
                        }
                    )
                }
        ) {
            // Lectura del estado de pulso de frame para suscribir el Canvas al redibujado
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            // Renderizado por capas
            drawGround(gameEngine.ground, width)

            val obstacles = gameEngine.obstacleManager.activeObstacles
            for (i in obstacles.indices) {
                drawObstacle(obstacles[i])
            }

            drawPlayer(gameEngine.player)
        }

        // HUD - Controles y Marcador de Puntuación
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botón de Pausa (Arriba Izquierda)
            IconButton(
                onClick = {
                    gameEngine.pauseGame()
                    onPauseClicked()
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.85f)),
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "ll",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF535353)
                )
            }

            // Marcador de Puntuación (Arriba Derecha)
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HI ${String.format(Locale.getDefault(), "%05d", gameEngine.highScore)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF747474)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = String.format(Locale.getDefault(), "%05d", gameEngine.currentScore.toInt()),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF535353)
                )
            }

            // Botón de Agacharse (Abajo Derecha)
            Button(
                onClick = {
                    gameEngine.onDuck(!gameEngine.player.isDucking)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameEngine.player.isDucking) Color(0xFF535353) else Color.White.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    text = if (gameEngine.player.isDucking) "DE PIE" else "AGACHARSE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameEngine.player.isDucking) Color.White else Color(0xFF535353)
                )
            }
        }
    }
}
