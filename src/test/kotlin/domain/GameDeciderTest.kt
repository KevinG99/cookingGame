package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
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
    private val gameDuration = GameDuration(BigDecimal.valueOf(1))
    private val gameCompletionTime = GameCompletionTime()
    private val gameIsSuccess = Success(true)
    private val gameIsNotSuccess = Success(false)
    private val gameScore = GameScore(100)

    @Test
    fun testCreateGame(): Unit = runTest {
        val createGameCommand = CreateGameCommand(gameId, gameName)
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(createGameCommand) // ACTION
            } thenEvents listOf(gameCreatedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCreateGame_Already_Exists_Error(): Unit = runTest {
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
    fun testPrepareGame(): Unit = runTest {
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
    fun testPrepareGame_Does_Not_Exist_Error(): Unit = runTest {
        val prepareGameCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(prepareGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPrepareGame_Not_IN_CREATION_STATE_ERROR(): Unit = runTest {
        val prepareGameCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameInOtherStateEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)

        val gameNotInCreatableStateEvent =
            GameNotInCorrectState(gameId, Error.GameNotInCorrectState.reason, GameStatus.PREPARED, true)
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
    fun testStartGame(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent)) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameStartedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGame_Does_Not_Exist_Error(): Unit = runTest {
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameDuration)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGame_Not_IN_PREPARED_STATE_ERROR(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val startGameCommand = StartGameCommand(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameNotInPreparedStateEvent =
            GameNotInCorrectState(gameId, Error.GameNotInCorrectState.reason, GameStatus.STARTED, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gameStartedEvent)) { // PRE CONDITIONS
                whenCommand(startGameCommand) // ACTION
            } thenEvents listOf(gameNotInPreparedStateEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGameTimer(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val startGameTimerCommand = StartGameTimerCommand(gameId)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)) { // PRE CONDITIONS
                whenCommand(startGameTimerCommand) // ACTION
            } thenEvents listOf(gameTimeElapsedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGameTimer_NotInCorrectStateError(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val startGameTimerCommand = StartGameTimerCommand(gameId)
        val gameNotInCorrectStateEvent =
            GameNotInCorrectState(gameId, Error.GameNotInCorrectState.reason, GameStatus.PREPARED, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent)) { // PRE CONDITIONS
                whenCommand(startGameTimerCommand) // ACTION
            } thenEvents listOf(gameNotInCorrectStateEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testStartGameTimer_GameDoesNotExistError(): Unit = runTest {
        val startGameTimerCommand = StartGameTimerCommand(gameId)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(startGameTimerCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_WHEN_TimeElapsed(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val completeGameCommand = CompleteGameCommand(gameId)
        val gameCompletedEvent = GameCompletedEvent(gameId, gameIsNotSuccess)
        with(gameDecider) {
            givenEvents(
                listOf(
                    gameCreatedEvent,
                    gamePreparedEvent,
                    gameStartedEvent,
                    gameTimeElapsedEvent
                )
            ) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameCompletedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_Not_IN_STARTED_STATE_ERROR(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val completeGameCommand = CompleteGameCommand(gameId)
        val gameNotInStartedStateEvent =
            GameNotInCorrectState(gameId, Error.GameNotInCorrectState.reason, GameStatus.PREPARED, true)
        with(gameDecider) {
            givenEvents(listOf(gameCreatedEvent, gamePreparedEvent)) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameNotInStartedStateEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_Does_Not_Exist_Error(): Unit = runTest {
        val completeGameCommand = CompleteGameCommand(gameId)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameDoesNotExistEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testCompleteGame_WHEN_GameEnded(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameEndedEvent = GameEndedEvent(gameId, gameScore)
        val completeGameCommand = CompleteGameCommand(gameId)
        val gameCompletedEvent = GameCompletedEvent(gameId, gameIsSuccess)
        with(gameDecider) {
            givenEvents(
                listOf(
                    gameCreatedEvent,
                    gamePreparedEvent,
                    gameStartedEvent,
                    gameEndedEvent
                )
            ) { // PRE CONDITIONS
                whenCommand(completeGameCommand) // ACTION
            } thenEvents listOf(gameCompletedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testEndGame() = runTest{
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameEndedEvent = GameEndedEvent(gameId, gameScore)
        val endGameCommand = EndGameCommand(gameId, gameScore, gameCompletionTime)
        with(gameDecider){
            givenEvents(
                listOf(
                    gameCreatedEvent,
                    gamePreparedEvent,
                    gameStartedEvent
                )
            ) {
                whenCommand(endGameCommand)
            } thenEvents listOf(
                gameEndedEvent
            )
        }
    }

    @Test
    fun testEndGame_Does_Not_Exist_Error() = runTest{
        val endGameCommand = EndGameCommand(gameId, gameScore, gameCompletionTime)
        val gameDoesNotExistEvent = GameDoesNotExistEvent(gameId, Error.GameDoesNotExist.reason, true)
        with(gameDecider){
            givenEvents(emptyList()){
                whenCommand(endGameCommand)
            } thenEvents listOf(
                gameDoesNotExistEvent
            )
        }
    }

    @Test
    fun testEndGame_Not_IN_STARTED_STATE_ERROR() = runTest{
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val endGameCommand = EndGameCommand(gameId, gameScore, gameCompletionTime)
        val gameNotInCorrectStateEvent = GameNotInCorrectState(gameId, Error.GameNotInCorrectState.reason, GameStatus.CREATED, true)
        with(gameDecider){
            givenEvents(listOf(gameCreatedEvent)){
                whenCommand(endGameCommand)
            } thenEvents listOf(
                gameNotInCorrectStateEvent
            )
        }
    }
}