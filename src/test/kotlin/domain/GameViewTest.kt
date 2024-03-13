package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest

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
    private val gameDuration = GameDuration(BigDecimal.valueOf(1))
    private val gameCompletionTime = GameCompletionTime()
    private val gameIsSuccess = Success(true)
    private val gameIsNotSuccess = Success(false)
    private val gameScore = GameScore(100)


    @Test
    fun testGameCreated(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gameViewState = GameViewState(gameId, gameName, gameCreatedEvent.status)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGamePrepared(): Unit = runTest {
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
    fun testGameStarted(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameViewState = GameViewState(
            gameId,
            gameName,
            gameStartedEvent.status,
            ingredientList,
            gameDuration,
            gameStartedEvent.startTime
        )
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameTimeElapsed(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameViewState = GameViewState(
            gameId,
            gameName,
            gameTimeElapsedEvent.status,
            ingredientList,
            gameDuration,
            gameStartedEvent.startTime
        )
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameCompletion_WHEN_GameTimeElapsed(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameViewState = GameViewState(
            gameId,
            gameName,
            gameTimeElapsedEvent.status,
            ingredientList,
            gameDuration,
            gameStartedEvent.startTime
        )
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameEnded(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameEndedEvent = GameEndedEvent(gameId)
        val gameViewState = GameViewState(
            gameId,
            gameName,
            gameEndedEvent.status,
            ingredientList,
            gameDuration,
            gameStartedEvent.startTime,
            completionTime = gameEndedEvent.completionTime
        )
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent, gameEndedEvent)
            ) thenState gameViewState
        }
    }

    // is IngredientPreparationCompletedEvent -> gameViewState?.let { state ->
    //                state.copy(
    //                    ingredients = IngredientList(
    //                        state.ingredients?.value?.map { ingredientItem ->
    //                            if (ingredientItem.id == gameEvent.ingredientId) {
    //                                ingredientItem.copy(
    //                                    status = gameEvent.ingredientStatus,
    //                                    preparationCompleteTime = gameEvent.preparationCompleteTime
    //                                )
    //                            } else ingredientItem
    //                        }?.toImmutableList()!!
    //                    )
    //                )
    //            }
    @Test
    fun `should Update Game Ingredient to IngredientStatus_PREPARED`(): Unit = runTest {
        val ingredientPreparationCompletedEvent =
            IngredientPreparationCompletedEvent(gameId, ingredientList.value.first().id)
        val updatedIngredientList = IngredientList(
            listOf(
                IngredientItem(
                    ingredientList.value.first().id,
                    ingredientList.value.first().name,
                    ingredientList.value.first().quantity,
                    ingredientList.value.first().inputTime,
                    IngredientStatus.PREPARED,
                    preparationCompleteTime = ingredientPreparationCompletedEvent.preparationCompleteTime
                )
            ).toImmutableList()
        )
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameViewState =
            GameViewState(gameId, gameName, gameStartedEvent.status, updatedIngredientList, gameDuration, gameStartedEvent.startTime)
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent,gameStartedEvent, ingredientPreparationCompletedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun `should Update Game Ingredient to IngredientStatus_ADDED`(): Unit = runTest {
           val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
            val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
            val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
            val ingredientPreparationCompletedEvent =
                IngredientPreparationCompletedEvent(gameId, ingredientList.value.first().id)
            val gameIngredientAdditionCompletedEvent =
                GameIngredientAdditionCompletedEvent(gameId, ingredientList.value.first().id)
            val updatedIngredientList = IngredientList(
                listOf(
                    IngredientItem(
                        ingredientList.value.first().id,
                        ingredientList.value.first().name,
                        ingredientList.value.first().quantity,
                        ingredientList.value.first().inputTime,
                        IngredientStatus.ADDED,
                        preparationCompleteTime = ingredientPreparationCompletedEvent.preparationCompleteTime,
                        additionCompleteTime = gameIngredientAdditionCompletedEvent.additionCompletedTimestamp
                    )
                ).toImmutableList()
            )
            val gameViewState =
                GameViewState(gameId, gameName, gameStartedEvent.status, updatedIngredientList, gameDuration, gameStartedEvent.startTime)
            with(gameView) {
                givenEvents(
                    listOf(
                        gameCreatedEvent,
                        gamePreparedEvent,
                        gameStartedEvent,
                        ingredientPreparationCompletedEvent,
                        gameIngredientAdditionCompletedEvent
                    )
                ) thenState gameViewState
            }
    }

    @Test
    fun `should calculate score`(): Unit = runTest {
        val gameCreatedEvent = GameCreatedEvent(gameId, gameName)
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val gameTimeElapsedEvent = GameTimeElapsedEvent(gameId)
        val gameEndedEvent = GameEndedEvent(gameId)
        val gameViewState = GameViewState(
            gameId,
            gameName,
            gameEndedEvent.status,
            ingredientList,
            gameDuration,
            gameStartedEvent.startTime,
            completionTime = gameEndedEvent.completionTime
        )
        with(gameView) {
            givenEvents(
                listOf(gameCreatedEvent, gamePreparedEvent, gameStartedEvent, gameTimeElapsedEvent, gameEndedEvent)
            ) thenState gameViewState
        }
    }
}