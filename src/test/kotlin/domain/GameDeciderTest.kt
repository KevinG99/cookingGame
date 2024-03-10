package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import thenEvents
import whenCommand
import java.math.BigDecimal


class GameDeciderTest {
    private val gameDecider = gameDecider()
    private val gameId = GameId()
    private val gameName = GameName("Test game")
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
    private val gameDuration = GameDuration(BigDecimal.valueOf(2))
    private val gameCompletionTime = GameCompletionTime()
    private val gameIsSuccess = Success(true)
    @Test
    fun testCreateGame(): Unit = runBlocking {
        val createGameCommand = CreateGameCommand(gameId, gameName)
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(createGameCommand) // ACTION
            } thenEvents listOf(gameCreatedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCreateGame_Already_Exists_Error(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val createGameCommand = CreateGameCommand(gameId, gameName)
        val gameAlreadyExistsEvent = GameAlreadyExistsEvent(gameId, Error.GameAlreadyExists.reason, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent)) { // PRE CONDITIONS
                whenCommand(createGameCommand) // ACTION
            } thenEvents listOf(gameAlreadyExistsEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPrepareGame(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val prepareGameCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent)) { // PRE CONDITIONS
                whenCommand(prepareGameCommand) // ACTION
            } thenEvents listOf(gamePreparedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPrepareGame_Does_Not_Exist_Error(): Unit = runBlocking {
        val prepareGameCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(prepareGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPrepareGame_Not_IN_CREATION_STATE_ERROR(): Unit = runBlocking {
        val prepareGameCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameInOtherStateEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)

        val gameNotInCreatableStateEvent =
            GameNotInCreatableStateEvent(gameId, Error.GameNotCreated.reason, true)

        with(gameDecider) {
            givenEvents(
                listOf(
                    gameCreatedEvent,
                    gameInOtherStateEvent
                )
            ) { // Set the game to a state other than 'CREATED'
                whenCommand(prepareGameCommand) // Attempt to prepare the game
            } thenEvents listOf(gameNotInCreatableStateEvent) // Check for the expected error event
        }
    }

    @Test
    fun testStartGame(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameStartTime, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList, gameStartTime, gameDuration)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent)) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameStartedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGame_Does_Not_Exist_Error(): Unit = runBlocking {
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameStartTime, gameDuration)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGame_Not_IN_PREPARED_STATE_ERROR(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameStartTime, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList, gameStartTime, gameDuration)
        val gameNotInPreparedStateEvent =
            GameNotInPreparedStateEvent(gameId, Error.GameNotPrepared.reason, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gameStartedEvent)) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameNotInPreparedStateEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testGameUpdate(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList, gameStartTime, gameDuration)
        val gameUpdateCommand = CheckGameTimerCommand(gameId)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)) { // PRE CONDITIONS
                whenCommand(gameUpdateCommand) // ACTION
            } thenEvents listOf(gameTimeElapsedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList, gameStartTime, gameDuration)
        val completeGameCommand = CompleteGameCommand(gameId, gameCompletionTime, gameIsSuccess)
        val gameCompletedEvent = GameCompletedEvent(gameId, gameCompletionTime, gameIsSuccess, GameScore(0))
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameCompletedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_Not_IN_STARTED_STATE_ERROR(): Unit = runBlocking {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val completeGameCommand = CompleteGameCommand(gameId, gameCompletionTime, gameIsSuccess)
        val gameNotInStartedStateEvent =
            GameNotInStartedStateEvent(gameId, Error.GameNotStarted.reason, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent)) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameNotInStartedStateEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_Does_Not_Exist_Error(): Unit = runBlocking {
        val completeGameCommand = CompleteGameCommand(gameId, gameCompletionTime, gameIsSuccess)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }
}