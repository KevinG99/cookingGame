package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import thenState
import java.math.BigDecimal

class GameViewTest {
    private val gameView = gameView()
    private val gameId = GameId()
    private val gameName = GameName("Test game")
    private val emptyIngredientList = null
    private val ingredientList = IngredientList(
        listOf(
            IngredientItem(
                IngredientId(),
                IngredientName("Test ingredient 1"),
                IngredientQuantity(5),
                IngredientInputTime(BigDecimal.TEN)
            )
        ).toImmutableList()
    )
    private val gameStartTime = GameStartTime()
    private val gameDuration = GameDuration(BigDecimal.valueOf(5))

    @Test
    fun testGameCreated(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameViewState = GameViewState(gameId, gameName, gamePreparedEvent.status, ingredientList, gameDuration)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameStarted(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameViewState = GameViewState(gameId, gameName, gameStartedEvent.status, ingredientList, gameDuration, gameStartedEvent.startTime)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameTimeElapsed(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameViewState = GameViewState(gameId, gameName, gameTimeElapsedEvent.status, ingredientList, gameDuration, gameStartedEvent.startTime)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameCompletion_WHEN_GameTimeElapsed(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameViewState = GameViewState(gameId, gameName, gameTimeElapsedEvent.status, ingredientList, gameDuration, gameStartedEvent.startTime)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent)
            ) thenState gameViewState
        }
    }
}