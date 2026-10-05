package com.example.practicando.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayerTest {

    private lateinit var player: Player

    @Before
    fun setUp() {
        player = Player(standWidth = 60f, standHeight = 70f)
        player.reset(startX = 100f, targetGroundY = 400f)
    }

    @Test
    fun playerInitialPositionOnGround() {
        assertEquals(100f, player.x, 0.01f)
        assertEquals(330f, player.y, 0.01f) // 400 - 70
        assertFalse(player.isJumping)
    }

    @Test
    fun jumpInitiatesUpwardVelocity() {
        val jumped = player.jump()
        assertTrue(jumped)
        assertTrue(player.isJumping)
        assertEquals(Player.JUMP_VELOCITY, player.velocityY, 0.01f)
    }

    @Test
    fun doubleJumpInMidAirIsPrevented() {
        player.jump()
        val initialVelocity = player.velocityY

        // Intentar saltar nuevamente en el aire
        val secondJump = player.jump()
        assertFalse(secondJump)
        assertEquals(initialVelocity, player.velocityY, 0.01f)
    }

    @Test
    fun gravityAndFlightArcCompletedWithLanding() {
        player.jump() // Empieza salto

        // Simular varios frames de vuelo
        var totalTime = 0f
        val dt = 0.016f // ~60 FPS frame

        while (player.isJumping && totalTime < 2f) {
            player.update(dt)
            totalTime += dt
        }

        // Al finalizar el arco de salto, debe aterrizar exactamente en el suelo
        assertFalse(player.isJumping)
        assertEquals(330f, player.y, 0.01f)
        assertEquals(0f, player.velocityY, 0.01f)
    }

    @Test
    fun duckingChangesDimensionsAndPositionOnGround() {
        player.setDucking(true)
        assertTrue(player.isDucking)
        assertEquals(70f * 0.55f, player.height, 0.01f)
        assertEquals(400f - player.height, player.y, 0.01f)

        player.setDucking(false)
        assertFalse(player.isDucking)
        assertEquals(70f, player.height, 0.01f)
        assertEquals(330f, player.y, 0.01f)
    }

    @Test
    fun hitboxIsInsetForFairCollision() {
        val hitbox = player.getHitbox()
        val insetX = player.width * 0.1f
        val insetY = player.height * 0.08f

        assertEquals(player.x + insetX, hitbox.left, 0.01f)
        assertEquals(player.y + insetY, hitbox.top, 0.01f)
        assertEquals(player.x + player.width - insetX, hitbox.right, 0.01f)
        assertEquals(player.y + player.height - insetY, hitbox.bottom, 0.01f)
    }
}
