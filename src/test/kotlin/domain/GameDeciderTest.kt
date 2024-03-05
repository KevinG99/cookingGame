package domain

import com.cookingGame.domain.*
import com.cookingGame.domain.gameDecider
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
    private val ingredientItems = listOf(
        IngredientItem(
            IngredientId(),
            IngredientName("Test ingredient 1"),
            IngredientQuantity(5),
            IngredientInputTime(BigDecimal.TEN)
        )
    ).toImmutableList()

    @Test
    fun testGenerateGame(): Unit = runBlocking{
        val generateGameCommand = GenerateGameCommand(gameId, gameName, ingredientItems)
        val gameGeneratedEvent = GameGeneratedEvent(gameId, gameName, ingredientItems)
        with(gameDecider){
            givenEvents(emptyList()){ // PRE CONDITIONS
                whenCommand(generateGameCommand) // ACTION
            } thenEvents listOf(gameGeneratedEvent) // POST CONDITIONS
        }
    }
}