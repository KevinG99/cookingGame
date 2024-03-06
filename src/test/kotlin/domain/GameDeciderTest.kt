package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import thenEvents
import whenCommand
import java.math.BigDecimal as BigDecimal1


class GameDeciderTest {
    private val gameDecider = gameDecider()
    private val gameId = GameId()
    private val gameName = GameName("Test game")
    private val ingredientItems = listOf(
        IngredientItem(
            IngredientId(),
            IngredientName("Test ingredient 1"),
            IngredientQuantity(5),
            IngredientInputTime(BigDecimal1.TEN)
        )
    ).toImmutableList()

    @Test
    fun testGenerateGame(): Unit = runBlocking {
        val generateGameCommand = GenerateGameCommand(gameId, gameName, ingredientItems)
        val gameGeneratedEvent = GameGeneratedEvent(gameId, gameName, ingredientItems)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(generateGameCommand) // ACTION
            } thenEvents listOf(gameGeneratedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPrepareGame(): Unit = runBlocking {
        val gamePreparationStartedEvent = GamePreparationStartedEvent(gameId, gameName)
        val startGamePreparationCommand = StartGamePreparationCommand(gameId, gameName)
        with(gameDecider) {
            givenEvents(emptyList()) { // PRE CONDITIONS
                whenCommand(startGamePreparationCommand) // ACTION
            } thenEvents listOf(gamePreparationStartedEvent) // POST CONDITIONS
        }
    }

    @Test
    fun testPreparationGameAlreadyExistsError(): Unit = runBlocking {
        val gamePreparationStartedEvent = GamePreparationStartedEvent(gameId, gameName)
        val generateGameCommand = StartGamePreparationCommand(gameId, gameName)
        val gameNotCreatedEvent = GameNotCreatedEvent(gameId, gameName, Reason("Game already exists"), true)
        with(gameDecider) {
            givenEvents(listOf(gamePreparationStartedEvent)) { // PRE CONDITIONS
                whenCommand(generateGameCommand) // ACTION
            } thenEvents listOf(gameNotCreatedEvent) // POST CONDITIONS
        }
    }
}