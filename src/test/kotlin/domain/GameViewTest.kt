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
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status, emptyIngredientList, null)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameCreated_AlreadyExists_Error(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameAlreadyExistsEvent = GameAlreadyExistsEvent(gameId, gameName, Error.GameAlreadyExists.reason, true)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status, emptyIngredientList, null)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gameAlreadyExistsEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, gameName, ingredientList, gameDuration)
        val gameViewState =
            GameViewState(gameId, gameName, gamePreparedEvent.status, ingredientList, null, gameDuration)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared_DoesNotExistGameError(): Unit = runBlocking {
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, gameName, Error.GameDoesNotExist.reason, true)
        val gameViewState = null
        with(gameView) {
            givenEvents(
                listOf(gameDoesNotExistEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared_NotInCreatableState_Error(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameNotInCreatableStateEvent =
            GameNotInCreatableStateEvent(gameId, gameName, Error.GameNotCreated.reason, true)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status, emptyIngredientList, null)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gameNotInCreatableStateEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameStarted(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, gameName, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, gameName, ingredientList, gameStartTime, gameDuration)
        val gameViewState = GameViewState(gameId, gameName, gameStartedEvent.status, ingredientList, gameStartTime, gameDuration)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameStarted_DoesNotExistGameError(): Unit = runBlocking {
        val gameStartedEvent = GameStartedEvent(gameId, gameName, ingredientList, gameStartTime, gameDuration)
        val gameViewState = null
        with(gameView) {
            givenEvents(
                listOf(gameStartedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameStarted_NotInPreparedState_Error(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameStartedEvent = GameStartedEvent(gameId, gameName, ingredientList, gameStartTime, gameDuration)
        val gameNotInPreparedStateEvent =
            GameNotInPreparedStateEvent(gameId, gameName, Error.GameNotPrepared.reason, true)
        val gameViewState = GameViewState(gameId, gameName, gameStartedEvent.status, emptyIngredientList, gameStartTime)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gameStartedEvent, gameNotInPreparedStateEvent)
            ) thenState gameViewState
        }
    }
}