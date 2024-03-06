package domain

import com.cookingGame.adapter.clients.GameClient
import com.cookingGame.domain.*
import expectActions
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import whenActionResult
import java.math.BigDecimal

class GameSagaTest {
    private val mockGameClient = mockk<GameClient>(relaxed = true)
    private val gameSaga = gameSaga(mockGameClient)
    private val gameId = GameId()
    private val gameName = GameName("something off")
    private val ingredientItems = listOf(
        IngredientItem(IngredientId(), IngredientName("ingredientItemName"), IngredientQuantity(5), IngredientInputTime(BigDecimal.TEN))
    ).toImmutableList()


    @Test
    fun testGamePreparationStartedEvent() = runBlocking {
        coEvery { mockGameClient.getIngredients(gameName) } returns flowOf(ingredientItems)
        val gamePreparationStartedEvent = GamePreparationStartedEvent(
            gameId,
            gameName
        )
        val startGamePreparationCommand = GenerateGameCommand(gameId, gameName, ingredientItems)

        with(gameSaga) {
            whenActionResult(
                gamePreparationStartedEvent
            ) expectActions listOf(startGamePreparationCommand)
        }
    }
}
