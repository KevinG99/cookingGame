package com.cookingGame.domain

import com.cookingGame.LOGGER
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class GameTimerManagerTest {
    private val gameId = GameId()
    private val gameName = GameName("Test game")
    private val gameStartTime = GameStartTime()
    private val gameDuration = GameDuration(BigDecimal.valueOf(5))

    @Test
    fun `test start timer for a single game`() = runBlocking {
        val game = Game(gameId, gameName, GameStatus.STARTED, gameDuration = gameDuration, startTime = gameStartTime)
        val gameFlow = GameTimerManager.startTimer(game)

        var timerElapsedEvent: GameTimeElapsedEvent? = null
        gameFlow.collect { event ->
            LOGGER.info("Received event: $event")
            if (event is GameTimeElapsedEvent) {
                timerElapsedEvent = event
            }
        }
        assertNotNull(timerElapsedEvent)
        assertEquals(game.id, timerElapsedEvent?.identifier)
    }

    @Test
    fun `test stop timer for a single game`() = runBlocking {
        val game = Game(gameId, gameName, GameStatus.STARTED, gameDuration = gameDuration, startTime = gameStartTime)
        val gameFlow = GameTimerManager.startTimer(game)

        var timerElapsedEvent: GameTimeElapsedEvent? = null
        delay(gameDuration.value.longValueExact()*1000 / 4)
        GameTimerManager.stopTimer(game.id)
        gameFlow.collect { event ->
            LOGGER.info("Received event: $event")
            if (event is GameTimeElapsedEvent) {
                timerElapsedEvent = event
            }
        }
        assertNull(timerElapsedEvent, "Timer elapsed event should not be emitted after stopping the timer")
    }
}