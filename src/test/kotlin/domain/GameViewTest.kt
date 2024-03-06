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
    private val ingredientItems = listOf(
        IngredientItem(
            IngredientId(),
            IngredientName("Test ingredient 1"),
            IngredientQuantity(5),
            IngredientInputTime(BigDecimal.TEN)
        )
    ).toImmutableList()

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
    fun testGameCreated_AlreadyExists_Error(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameAlreadyExistsEvent = GameAlreadyExistsEvent(gameId, gameName, Error.GameAlreadyExists.reason, true)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gameAlreadyExistsEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, gameName)
        val gameViewState = GameViewState(gameId, gameName, gamePreparedEvent.status)
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
        val gameNotInCreatableStateEvent = GameNotInCreatableStateEvent(gameId, gameName, Error.GameNotCreated.reason, true)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gameNotInCreatableStateEvent)
            ) thenState gameViewState
        }
    }
}