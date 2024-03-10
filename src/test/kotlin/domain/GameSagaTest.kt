package domain

import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.domain.*
import expectActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import whenActionResult
import java.math.BigDecimal

class GameSagaTest {
    private val mockGameClient = mockk<GameClient>(relaxed = true)
    private val gameSaga = gameSaga(mockGameClient)
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
    fun testGamePreparationStartedEvent() = runTest {
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
    fun testStartGameTimerCommand() = runTest{
        val gameStartedEvent = GameStartedEvent(gameId, ingredientList)
        val startGameTimerCommand = StartGameTimerCommand(gameId)
        with(gameSaga) {
            whenActionResult(
                gameStartedEvent
            ) expectActions listOf(startGameTimerCommand)
        }
    }
}
