package domain

import com.cookingGame.LOGGER
import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.application.GameService
import com.cookingGame.domain.*
import expectActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import whenActionResult
import java.math.BigDecimal

class GameSagaTest {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
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

    private val gameDuration = GameDuration(BigDecimal.valueOf(10))
    private val gameStartTime = GameStartTime()
    private val ollamaResponse = OllamaResponse(gameDuration, ingredientList)

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
            GameStartTime(Clock.System.now().plus(2, DateTimeUnit.SECOND)),
            GameDuration(BigDecimal.valueOf(3)))
            val checkGameTimerCommand = CheckGameTimerCommand (gameId)
        LOGGER.info("First Game started: ${gameStartedEvent.startTime.value}")
        LOGGER.info("Second Game started: ${secondGameStartedEvent.startTime.value}")
        val checkGameTimerCommand2 = CheckGameTimerCommand(secondGameStartedEvent.identifier)
        with(gameSaga) {

            whenActionResult(
                gameStartedEvent
            ) expectActions listOf(checkGameTimerCommand)
            whenActionResult(
                secondGameStartedEvent
            ) expectActions listOf(checkGameTimerCommand2)
        }
    }
}
