package domain

import com.cookingGame.domain.*
import givenEvents
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import thenEvents
import whenCommand
import java.math.BigDecimal


class IngredientDeciderTest {

    private val ingredientDecider = ingredientDecider()
    private val gameId = GameId()
    private val ingredientId = IngredientId()
    private val ingredientName = IngredientName("ingredientName")
    private val quantity = IngredientQuantity(10)
    private val inputTime = IngredientInputTime(BigDecimal.TEN)


    @Test
    fun `should create ingredient`() = runTest{
        val initalizeIngredientCommand = InitalizeIngredientCommand(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        with(ingredientDecider) {
            givenEvents(emptyList()) {
                whenCommand(initalizeIngredientCommand)
            } thenEvents listOf(ingredientInitializedEvent)
        }
    }

    @Test
    fun `should not create ingredient if already exists`() = runTest{
        val initalizeIngredientCommand = InitalizeIngredientCommand(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        val ingredientInitializedEvent = IngredientInitializedEvent(
            ingredientId,
            gameId,
            ingredientName,
            quantity,
            inputTime
        )
        with(ingredientDecider) {
            givenEvents(listOf(ingredientInitializedEvent)) {
                whenCommand(initalizeIngredientCommand)
            } thenEvents listOf(IngredientAlreadyExistsEvent(ingredientId, Error.IngredientAlreadyExists.reason))
        }
    }
}