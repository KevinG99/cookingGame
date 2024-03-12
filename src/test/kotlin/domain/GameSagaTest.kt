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
    private val ingredientId = IngredientId()
    private val ingredientName = IngredientName("Test ingredient 1")
    private val ingredientQuantity = IngredientQuantity(5)
    private val ingredientInputTime = IngredientInputTime(BigDecimal.TEN)
    @Test
    fun `should prepare game`() = runTest {
        coEvery { mockGameClient.getIngredients(gameName) } returns flowOf(ollamaResponse)
        val gameCreatedEvent = GameCreatedEvent(
            gameId,
            gameName
        )
        val gamePreparationCommand = PrepareGameCommand(gameId, ingredientList, gameDuration)

        with(gameSaga) {
            whenActionResult(
                gameCreatedEvent
            ) expectActions listOf(gamePreparationCommand)
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

    @Test
    fun `should initialize ingredient`() = runTest {
        val gamePreparedEvent = GamePreparedEvent(gameId, ingredientList, gameDuration)
        val initalizeIngredientCommands = ingredientList.value.map { (id, name, quantity, inputTime   ) ->
            InitalizeIngredientCommand(
                id,
                gameId,
                name,
                quantity,
                inputTime
            )
        }
        with(gameSaga) {
            whenActionResult(
                gamePreparedEvent
            ) expectActions initalizeIngredientCommands
        }
    }

    @Test
    fun `should updateGameIngredientCommand`() = runTest {
        val initializedIngredientEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            ingredientQuantity,
            ingredientInputTime
        )
        val updateGameIngredientCommand = UpdateGameIngredientCommand(gameId, initializedIngredientEvent.identifier, initializedIngredientEvent.status )
        with(gameSaga) {
            whenActionResult(
                initializedIngredientEvent
            ) expectActions listOf(updateGameIngredientCommand)
        }
    }
}
