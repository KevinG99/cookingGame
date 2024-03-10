package com.cookingGame.domain

import com.cookingGame.LOGGER
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class GameTimerManagerTest {
    private val gameId = GameId()
    private val gameName = GameName("Test game")
    private val gameStartTime = GameStartTime()
    private val gameDuration = GameDuration(BigDecimal.valueOf(1))
    private val game =
        Game(gameId, gameName, GameStatus.STARTED, gameDuration = gameDuration, startTime = gameStartTime)

    @Test
    fun `startTimer should emit GameTimeElapsedEvent`() = runBlocking {
        val events = mutableListOf<GameEvent>()
        GameTimerManager.startTimer(game).take(1).collect { gameEvent ->
            LOGGER.info(gameEvent.toString())
            events.add(gameEvent)
            assertTrue(events.any { it is GameTimeElapsedEvent })
        }
    }

    @Test
    fun `stopTimer should stop the game timer`() = runBlocking {
        val events = mutableListOf<GameEvent>()
        val eventFlow = GameTimerManager.startTimer(game).take(1)
        println(Clock.System.now())
        launch {
            delay(gameDuration.value.longValueExact() * 1000 / 2)
            GameTimerManager.stopTimer(game.id)
        }

        eventFlow.collect { gameEvent ->
            events.add(gameEvent)
            assertFalse(events.any { it is GameTimeElapsedEvent })
        }
    }


    @Test
    fun `start multiple timers should emit multiple GameTimeElapsedEvent`() = runBlocking {
        val channel = Channel<GameEvent>(Channel.UNLIMITED)
        launch {
            val eventFlow = GameTimerManager.startTimer(game).take(1)
            eventFlow.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }

        launch {
            val eventFlow2 = GameTimerManager.startTimer(game).take(1)
            eventFlow2.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }
        delay(gameDuration.value.longValueExact() * 1000 + 500)
        val eventList = mutableListOf<GameEvent>()
        while (!channel.isEmpty) {
            eventList.add(channel.receive())
        }

        channel.close()
        assertTrue(eventList.size >= 2 && eventList.all { it is GameTimeElapsedEvent }, "Should have at least two GameTimeElapsedEvent")
    }

    @Test
    fun `stopTimer should stop the game timer for a specific game`() = runBlocking {
        val game2 = game.copy(id = GameId())
        val channel = Channel<GameEvent>(Channel.UNLIMITED)
        launch {
            val eventFlow = GameTimerManager.startTimer(game).take(1)
            eventFlow.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }

        launch {
            val eventFlow2 = GameTimerManager.startTimer(game2).take(1)
            eventFlow2.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }
        delay(gameDuration.value.longValueExact() * 1000 / 2)
        GameTimerManager.stopTimer(game.id)
        delay(gameDuration.value.longValueExact() * 1000 + 500)
        val eventList = mutableListOf<GameEvent>()
        while (!channel.isEmpty) {
            eventList.add(channel.receive())
        }

        channel.close()
        assertTrue(eventList.size ==1  && eventList.all { it is GameTimeElapsedEvent }, "Should have only one GameTimeElapsedEvent")
    }
}