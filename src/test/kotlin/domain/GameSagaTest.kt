package domain

import com.cookingGame.LOGGER
import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.application.GameService
import com.cookingGame.domain.*
import expectActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import whenActionResult
import java.math.BigDecimal

class GameSagaTest {
    private val mockGameClient = mockk<GameClient>(relaxed = true)
    private val gameService = GameService()
    private val gameSaga = gameSaga(mockGameClient, gameService)
    private val gameId = GameId()
    private val gameName = GameName("something off")
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

    private val gameDuration = GameDuration(BigDecimal.valueOf(5))
    private val gameStartTime = GameStartTime()
    private val ollamaResponse = OllamaResponse(gameDuration, ingredientList)
    private val gameCompletionTime = GameCompletionTime()
    private val gameIsSuccess = Success(true)
    @Test
    fun testGamePreparationStartedEvent() = runBlocking {
        coEvery { mockGameClient.getIngredients(gameName) } returns flowOf(ollamaResponse)
        val gameCreatedEvent = GameCreatedEvent(
            gameId,
            gameName
        )
        val startGamePreparationCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)

        with(gameSaga) {
            whenActionResult(
                gameCreatedEvent
            ) expectActions listOf(startGamePreparationCommand)
        }
    }

    @Test
    fun testStartedEvent_Should_ElapseTime() = runBlocking {
        val gameStartedEvent = GameStartedEvent(
            gameId,
            ingredientList,
            gameStartTime,
            gameDuration
        )
        val secondGameStartedEvent = GameStartedEvent(
            GameId(),
            ingredientList,
            GameStartTime(gameStartTime.value.plus(1, DateTimeUnit.SECOND)),
            GameDuration(gameDuration.value.minus(1.toBigDecimal())))

        val checkGameTimerCommand = CheckGameTimerCommand(gameId)
        val checkGameTimerCommand2 = CheckGameTimerCommand(secondGameStartedEvent.identifier)
        with(gameSaga) {
            launch {
                LOGGER.info("First Game started: ${gameStartedEvent.startTime.value},$gameId")
                whenActionResult(
                    gameStartedEvent
                ) expectActions listOf(checkGameTimerCommand)
            }
            LOGGER.info("Second Game started: ${secondGameStartedEvent.startTime.value}, ${secondGameStartedEvent.identifier}")
            whenActionResult(
                secondGameStartedEvent
            ) expectActions listOf(checkGameTimerCommand2)
        }
    }

    @Test
    fun testGameCompletionEvent() = runBlocking {
        val gameCompletedEvent = GameCompletedEvent(
            gameId,
            gameCompletionTime,
            gameIsSuccess,
            GameScore(0)
        )
        with(gameSaga) {
            whenActionResult(
                gameCompletedEvent
            ) expectActions listOf()
        }
    }
}
