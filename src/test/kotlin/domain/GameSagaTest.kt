package domain

import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.adapter.persistence.GameRepository
import com.cookingGame.adapter.persistence.IngredientRepository
import com.cookingGame.domain.*
import expectActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import whenActionResult
import java.math.BigDecimal

class GameSagaTest {
    private val mockGameClient = mockk<GameClient>(relaxed = true)
    private val mockIngredientRepository = mockk<IngredientRepository>()
    private val mockGameRepository = mockk<GameRepository>()
    private val gameSaga = gameSaga(mockGameClient, mockIngredientRepository, mockGameRepository)
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
    private val ingredientQuantity = IngredientQuantity(2)
    private val ingredientInputTime = IngredientInputTime(BigDecimal.TEN)
    private val ingredientPreparationTimestampList = mutableListOf<IngredientPreparationTimestamp>()
    private val ingredientAddedTimestampList = mutableListOf<IngredientAddedTimestamp>()

    private val preparedIngredientViewState = IngredientViewState(
        ingredientId,
        gameId,
        ingredientName,
        IngredientQuantity(2),
        ingredientInputTime,
        IngredientStatus.PREPARED
    )
    private val gameViewState = GameViewState(
        gameId,
        gameName,
        GameStatus.CREATED,
        ingredientList,
        gameDuration,
        gameStartTime,
    )

    @BeforeEach
    fun setUp() {
        coEvery { mockGameClient.getIngredients(gameName) } returns flowOf(ollamaResponse)
    }

    @Test
    fun `should prepare game`() = runTest {
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
    fun testStartGameTimerCommand() = runTest {
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
        val initalizeIngredientCommands = ingredientList.value.map { (id, name, quantity, inputTime) ->
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
        val updateGameIngredientCommand = UpdateGameIngredientCommand(
            gameId,
            initializedIngredientEvent.identifier,
            initializedIngredientEvent.status
        )
        with(gameSaga) {
            whenActionResult(
                initializedIngredientEvent
            ) expectActions listOf(updateGameIngredientCommand)
        }
    }

    @Test
    fun `should complete ingredient preparation`() = runTest {
        repeat(6) {
            ingredientPreparationTimestampList.add(IngredientPreparationTimestamp())
        }
        val newPreparedIngredientViewState = preparedIngredientViewState.copy(
            quantity = IngredientQuantity(5),
            preparationTimestamps = IngredientPreparationList(
                ingredientPreparationTimestampList.toImmutableList()
            )
        )
        coEvery { mockIngredientRepository.findById(ingredientId.value.toString()) } returns newPreparedIngredientViewState
        val ingredientPreparedEvent = IngredientPreparedEvent(ingredientId)
        val completeIngredientPreparationCommand = CompleteIngredientPreparationCommand(gameId, ingredientId)
        with(gameSaga) {
            whenActionResult(
                ingredientPreparedEvent
            ) expectActions listOf(completeIngredientPreparationCommand)
        }
    }

    @Test
    fun `should not complete ingredient preparation`() = runTest {
        repeat(4) {
            ingredientPreparationTimestampList.add(IngredientPreparationTimestamp())
        }
        val preparedIngredientViewStateNotEnoughPrepared = preparedIngredientViewState.copy(
            quantity = IngredientQuantity(5),
            preparationTimestamps = IngredientPreparationList(
                ingredientPreparationTimestampList.toImmutableList()
            )
        )
        coEvery { mockIngredientRepository.findById(ingredientId.value.toString()) } returns preparedIngredientViewStateNotEnoughPrepared
        val ingredientPreparedEvent = IngredientPreparedEvent(ingredientId)
        with(gameSaga) {
            whenActionResult(
                ingredientPreparedEvent
            ) expectActions listOf()
        }
    }

    @Test
    fun `should add ingredient to game`() = runTest {
        ingredientAddedTimestampList.add(IngredientAddedTimestamp())
        val newPreparedIngredientViewState = preparedIngredientViewState.copy(
            addedTimestamps = IngredientAddedList(
                ingredientAddedTimestampList.toImmutableList()
            )
        )
        coEvery { mockIngredientRepository.findById(ingredientId.value.toString()) } returns newPreparedIngredientViewState
        val ingredientAddedEvent = IngredientAddedEvent(ingredientId)
        val addIngredientToGameCommand = AddIngredientToGameCommand(gameId, ingredientId)
        with(gameSaga) {
            whenActionResult(
                ingredientAddedEvent
            ) expectActions listOf(addIngredientToGameCommand)
        }
    }


    @Test
    fun `should stop gameCommand`() = runTest {
        val newIngredientItemList = ingredientList.value.map { it.copy(status = IngredientStatus.ADDED) }
        val newGameViewState = gameViewState.copy(
            ingredients = IngredientList(newIngredientItemList.toImmutableList())
        )
        coEvery { mockGameRepository.findById(gameId.value.toString()) } returns newGameViewState
        val gameIngredientAdditionCompletedEvent = GameIngredientAdditionCompletedEvent(gameId, ingredientId)
        val endGameCommand = EndGameCommand(gameId)
        with(gameSaga) {
            whenActionResult(
                gameIngredientAdditionCompletedEvent
            ) expectActions listOf(endGameCommand)
        }
    }

    @Test
    fun `should calculate score command`() = runTest {
        val gameEndedEvent = GameEndedEvent(gameId)
        val ingredientViewStates = listOf(preparedIngredientViewState)
        coEvery { mockIngredientRepository.findAllByGameId(gameId.value.toString()) } returns ingredientViewStates
        val scoreCalculationCommand = CalculateScoreCommand(gameId, ScoreCalculationInput(ingredientViewStates))
        with(gameSaga) {
            whenActionResult(
                gameEndedEvent
            ) expectActions listOf(scoreCalculationCommand)
        }
    }
}
