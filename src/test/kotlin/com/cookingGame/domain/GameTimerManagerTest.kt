package com.cookingGame.domain

import com.cookingGame.LOGGER
import io.mockk.mockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class GameTimerManagerTest {
    private val gameTimeManager = GameTimerManager
    private val gameId = GameId()
    private val gameName = GameName("Test game")
    private val gameStartTime = GameStartTime()
    private val gameDuration = GameDuration(BigDecimal.valueOf(2))
    private val game =
        Game(gameId, gameName, GameStatus.STARTED, gameDuration = gameDuration, startTime = gameStartTime)

    @BeforeEach
    fun setUp() {
        mockkObject(GameTimerManager)
    }

    @Test
    fun `startTimer should emit GameTimeElapsedEvent`() = runTest {
        val events = mutableListOf<GameEvent>()
        gameTimeManager.startTimer(game).take(1).collect { gameEvent ->
            LOGGER.info(gameEvent.toString())
            events.add(gameEvent)
            assertTrue(events.any { it is GameTimeElapsedEvent })
        }
    }

    @Test
    fun `stopTimer should stop the game timer`() = runTest {
        val events = mutableListOf<GameEvent>()
        val eventFlow = gameTimeManager.startTimer(game).take(1)
        println(Clock.System.now())
        launch {
            delay(gameDuration.value.longValueExact() * 1000 / 2)
            gameTimeManager.stopTimer(game.id)
        }

        eventFlow.collect { gameEvent ->
            events.add(gameEvent)
            LOGGER.info(events.toString())
            assertTrue(events.isEmpty(), "Should not emit any GameTimeElapsedEvent")
        }
    }


    @Test
    fun `start multiple timers should emit multiple GameTimeElapsedEvent`() = runBlocking {
        val channel = Channel<GameEvent>(Channel.UNLIMITED)
        launch {
            val eventFlow = gameTimeManager.startTimer(game).take(1)
            eventFlow.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }

        launch {
            val eventFlow2 = gameTimeManager.startTimer(game).take(1)
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
            val eventFlow = gameTimeManager.startTimer(game).take(1)
            eventFlow.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }

        launch {
            val eventFlow2 = gameTimeManager.startTimer(game2).take(1)
            eventFlow2.collect { gameEvent ->
                channel.send(gameEvent)
            }
        }
        delay(gameDuration.value.longValueExact() * 1000 / 2)
        gameTimeManager.stopTimer(game.id)
        delay(gameDuration.value.longValueExact() * 1000 + 500)
        val eventList = mutableListOf<GameEvent>()
        while (!channel.isEmpty) {
            eventList.add(channel.receive())
        }

        channel.close()
        val expected = listOf(GameTimeElapsedEvent(game.id))
        assertTrue(eventList.size ==1  && eventList.all { it is GameTimeElapsedEvent }, "expect: $expected but was: $eventList")
    }
}