package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
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
    fun testGameGenerated(): Unit {
        val gameGeneratedEvent = GameGeneratedEvent(gameId, gameName, ingredientItems)
        val gameViewState = GameViewState(gameId, gameName, ingredientItems)
        with(gameView) {
            givenEvents(
                listOf(gameGeneratedEvent)
            ) thenState gameViewState
        }
    }

    @Test
    fun testGameNotGenerated(): Unit {
        val gameGeneratedEvent = GameGeneratedEvent(gameId, gameName, ingredientItems)
        val gameNotCreatedEvent = GameNotCreatedEvent(gameId, gameName, Reason("Game already exists"), true)
        val gameViewState = GameViewState(gameId, gameName, ingredientItems)
        with(gameView) {
            givenEvents(
                listOf(gameNotCreatedEvent, gameGeneratedEvent)
            ) thenState gameViewState
        }
    }

}